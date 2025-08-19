package skytales.Carts.util.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import skytales.Carts.model.BookItemReference;


import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@EnableAsync
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    long TTL_SECONDS = 70;

    private final String CARTS_ZSET_KEY = "cart_terms";

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jacksonSerializer);
    }

    public Set<BookItemReference> get(String key) {
        Object data = redisTemplate.opsForValue().get(key);
        if (data == null) return null;

        if (data instanceof LinkedHashMap) {
            return new HashSet<>();
        }

        return new HashSet<>((Collection<? extends BookItemReference>) data);
    }

    public void set(String key, Set<BookItemReference> value) {
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, TTL_SECONDS, TimeUnit.SECONDS);
    }

    public int getTerm(String versionKey) {
        Double score = redisTemplate.opsForZSet().score(CARTS_ZSET_KEY, versionKey);
        if (score == null) return 0;
        return score.intValue();
    }

    public void incrBy(String versionKey) {
        if (redisTemplate.opsForZSet().score(CARTS_ZSET_KEY, versionKey) == null) {
            redisTemplate.opsForZSet().add(CARTS_ZSET_KEY, versionKey, 0);
        }
        redisTemplate.opsForZSet().incrementScore(CARTS_ZSET_KEY, versionKey, 1);
    }

    public void reset(String versionKey) {
        redisTemplate.opsForZSet().add(CARTS_ZSET_KEY, versionKey, 0);
    }

    public void delete(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Async
    public void checkAndCleanMemory() {
        if (isMemoryFull()) {
         throw new RedisConnectionFailureException("Memory is already full");
        }
    }

    public boolean isMemoryFull() {
        try {

            RedisConnection connection = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection();

            String memoryInfo = String.valueOf(connection.info("memory"));

            String[] lines = memoryInfo.split("\n");
            long usedMemoryRss = 0;

            for (String line : lines) {
                if (line.startsWith("used_memory_rss:")) {
                    usedMemoryRss = Long.parseLong(line.split(":")[1].trim());
                    break;
                }
            }

            long memoryThreshold = 1_073_741_824;

            if( usedMemoryRss > 0.8 * memoryThreshold) {
             boolean cleanup =  checkAndEvictInactiveCarts(50);

             if(!cleanup) {
               return  checkAndEvictLeastUsedCarts(50);
             }

             return cleanup;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean checkAndEvictInactiveCarts(int limit) {
        String luaScript =
                "local limit = ARGV[1]\n" +
                        "local evictedCarts = {}\n" +
                        "local inactiveCarts = redis.call('ZRANGE', 'cart_activity', 0, limit - 1)\n" +
                        "for _, cartId in ipairs(inactiveCarts) do\n" +
                        "    local cartKey = 'shopping_cart:' .. cartId\n" +
                        "    local versionKey = 'cartVersion:' .. cartId\n" +
                        "    local term = tonumber(redis.call('GET', versionKey)) or 0\n" +
                        "    if term == 0 then\n" +
                        "        redis.call('DEL', cartKey)\n" +
                        "        redis.call('ZREM', 'cart_activity', cartId)\n" +
                        "        table.insert(evictedCarts, cartId)\n" +
                        "    end\n" +
                        "end\n" +
                        "return evictedCarts";

        RedisScript<List> script = RedisScript.of(luaScript, List.class);

        try {
            List<String> evictedCarts = redisTemplate.execute(script, Collections.singletonList("cart_activity"), limit);
            return evictedCarts == null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean checkAndEvictLeastUsedCarts(int limit) {
        String luaScript =
                "local limit = ARGV[1]\n" +
                        "local evictedCarts = {}\n" +
                        "local cartsToEvict = redis.call('ZRANGE', KEYS[1], 0, limit-1, 'WITHSCORES')\n" +
                        "for i = 1, #cartsToEvict, 2 do\n" +
                        "   local cartId = cartsToEvict[i]\n" +
                        "   local cartKey = 'shopping_cart:' .. cartId\n" +
                        "   redis.call('DEL', cartKey)\n" +
                        "   table.insert(evictedCarts, cartId)\n" +
                        "end\n" +
                        "local evictedCartsStr = table.concat(evictedCarts, ',')\n" +
                        "redis.call('PUBLISH', 'syncChannel', 'syncRequest:' .. evictedCartsStr)\n" +
                        "return evictedCartsStr";

        RedisScript<List> script = RedisScript.of(luaScript, List.class);

        try {
            List<String> evictedCarts = redisTemplate.execute(script,  List.of(CARTS_ZSET_KEY), limit);
            return evictedCarts == null;
        } catch (Exception e) {
            return false;
        }
    }

}

