package skytales.Carts.util.redis.sync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import skytales.Carts.model.BookItemReference;
import skytales.Carts.model.Cart;
import skytales.Carts.util.redis.RedisService;
import skytales.Carts.repository.CartRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@EnableAsync
@Service
@EnableKafka
public class CartBatchSync {


    private final RedisTemplate<String, Object> redisTemplate;
    private final CartRepository cartRepository;
    private final RedisService redisService;

    public CartBatchSync(RedisTemplate<String, Object> redisTemplate, CartRepository cartRepository, RedisService redisService) {
        this.redisTemplate = redisTemplate;
        this.cartRepository = cartRepository;
        this.redisService = redisService;
    }

    @Async
    public void syncCartsBatch() {
        log.info("Starting syncCartsBatch process");

        try {
            Set<String> cartKeys = redisTemplate.keys("shopping_cart:*");
            log.info("Retrieved cart keys: {}", cartKeys);

            if (cartKeys != null) {
                for (String cartKey : cartKeys) {
                    Set<BookItemReference> cachedCart = redisService.get(cartKey);
                    log.info("cachedCart type: {}", cachedCart != null ? cachedCart.getClass().getName() : "null");
                    log.info("cachedCart content: {}", cachedCart);

                    if (cachedCart != null) {
                        String cartId = cartKey.split(":")[1];
                        String versionKey = "cartVersion:" + cartId;
                        int term = redisService.getTerm(versionKey);
                        log.info("cartId: {}, term: {}", cartId, term);

                        if (term == 0) {
                            log.info("Term is 0, skipping cartId: {}", cartId);
                            continue;
                        }

                        UUID id = UUID.fromString(cartId);
                        Cart cart = cartRepository.findById(id).orElseThrow(RuntimeException::new);
                        log.info("cart type: {}", cart.getClass().getName());
                        log.info("cart books type: {}", cart.getBooks().getClass().getName());
                        log.info("cart books content: {}", cart.getBooks());

                        Set<BookItemReference> cartBooksSet = new HashSet<>(cart.getBooks());

                        if (!cachedCart.equals(cartBooksSet)) {
                            log.info("Differences found, updating cartId: {}", cartId);
                            cart.setBooks(cartBooksSet);
                            cartRepository.save(cart);
                            redisService.reset(versionKey);
                            log.info("Updated and saved cartId: {}", cartId);
                        } else {
                            log.info("No differences found for cartId: {}", cartId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error during syncCartsBatch process", e);
        }

        log.info("Completed syncCartsBatch process");
    }
}

