package skytales.Carts.redis.sync;//package skytales.cart.redis.sync;
//
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import skytales.cart.redis.RedisHealthChecker;
//
//import java.util.List;
//import java.util.Set;
//
//@RestController
//@RequestMapping("/api/health-monitor")
//public class HealthMonitor {
//
//
//    private final CartBatchSync cartBatchSync;
//    private final CacheStartupSync cacheStartupSync;
//    private final RedisHealthChecker redisHealthChecker;
//
//    public HealthMonitor(CartBatchSync cartBatchSync, CacheStartupSync cacheStartupSync, RedisHealthChecker redisHealthChecker) {
//        this.cartBatchSync = cartBatchSync;
//        this.cacheStartupSync = cacheStartupSync;
//        this.redisHealthChecker = redisHealthChecker;
//    }
//
//    @Async
//    @GetMapping("/disconnected")
//    public void disconnected(@RequestBody Set<String> keys) {
////        cartBatchSync.syncCartsBatch(keys);
//    }
//
//    @Async
//    @GetMapping("/connected")
//    public void connected() {
//        cacheStartupSync.syncCache();
//    }
//}
