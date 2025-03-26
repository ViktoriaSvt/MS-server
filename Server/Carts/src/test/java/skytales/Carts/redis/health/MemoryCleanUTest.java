package skytales.Carts.redis.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;
import skytales.Carts.util.redis.RedisService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemoryCleanUTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private RedisConnection connection;

    @Mock
    private RedisConnectionFactory connectionFactory;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @InjectMocks
    private RedisService redisService;

    private static final long MEMORY_THRESHOLD = 1_073_741_824L;
    private static final String CARTS_ZSET_KEY = "cart_terms";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        lenient().when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        lenient().when(connectionFactory.getConnection()).thenReturn(connection);
    }

    @Test
    void testMemoryBelowThreshold() {

        boolean result = redisService.isMemoryFull();
        assertFalse(result, "Memory cleanup should not be triggered when memory usage is below threshold");
    }


    @Test
    void testMemoryAboveThresholdWithFailedCleanup() {

        RedisService spyService = spy(redisService);
        boolean result = spyService.isMemoryFull();

        assertFalse(result, "Memory cleanup should fail when both eviction methods fail");
    }

    @Test
    void testRedisConnectionFailure() {
       lenient().when(connection.info("memory")).thenThrow(RedisConnectionFailureException.class);

        boolean result = redisService.isMemoryFull();
        assertFalse(result, "Memory cleanup should not be triggered when Redis connection fails");
    }

    @Test
    void testCheckAndEvictInactiveCarts() {
        String luaScript = "local limit = ARGV[1]\n" +
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
        List<String> evictedCarts = List.of("cart1", "cart2");

        lenient().when(redisTemplate.execute(script, Collections.singletonList("cart_activity"), 50)).thenReturn(evictedCarts);

        boolean result = redisService.checkAndEvictInactiveCarts(50);

        assertTrue(result, "Eviction of inactive carts should return true if carts were evicted");
    }

    @Test
    void testCheckAndEvictLeastUsedCarts() {
        String luaScript = "local limit = ARGV[1]\n" +
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
        List<String> evictedCarts = List.of("cart1", "cart2");

        lenient().when(redisTemplate.execute(script, List.of(CARTS_ZSET_KEY), 50)).thenReturn(evictedCarts);

        boolean result = redisService.checkAndEvictLeastUsedCarts(50);

        assertTrue(result, "Eviction of least used carts should return true if carts were evicted");
    }
}