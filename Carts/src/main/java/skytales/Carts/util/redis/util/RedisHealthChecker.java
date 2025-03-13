package skytales.Carts.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import skytales.Carts.state_engine.UpdateProducer;


@EnableAsync
@Getter
@Setter
@Component
@EnableScheduling
public class RedisHealthChecker {


    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private final RedisService redisService;

    private volatile boolean redisAvailable = false;
    @Autowired
    private UpdateProducer updateProducer;

    public RedisHealthChecker( RedisTemplate<String, Object> redisTemplate, RedisService redisService) {

        this.redisTemplate = redisTemplate;
        this.redisService = redisService;
    }

    @Async
    @Scheduled(fixedRate = 120000)
    public void checkRedisHealth() {

        try {
            if (!redisAvailable) {
                System.out.println("✅ Redis is synced and ready.");
                redisAvailable = true;
                return;
            }

            redisService.checkAndCleanMemory();
            updateProducer.sendBatchSyncRequest();
        } catch (Exception e) {
            if (redisAvailable) {
                System.out.println("❌ Redis is down.");
                redisAvailable = false;
            }

        }
    }


}
