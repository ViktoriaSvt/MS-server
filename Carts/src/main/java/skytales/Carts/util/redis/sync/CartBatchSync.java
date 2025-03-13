package skytales.Carts.redis.sync;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.stereotype.Service;
import skytales.Carts.model.BookItemReference;
import skytales.Carts.model.Cart;
import skytales.Carts.redis.RedisService;
import skytales.Carts.repository.CartRepository;


import java.util.Set;
import java.util.UUID;

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

    public void syncCartsBatch() {

        try {
             Set<String> cartKeys = redisTemplate.keys("shopping_cart:*");

            if (cartKeys != null) {
                for (String cartKey : cartKeys) {
                    Set<BookItemReference> cachedCart = redisService.get(cartKey);

                    if (cachedCart != null) {
                        String cartId = cartKey.split(":")[1];
                        String versionKey = "cartVersion:" + cartId;
                        int term = redisService.getTerm(versionKey);

                        if (term == 0) {
                            return;
                        }

                        Cart cart = cartRepository.findById(UUID.fromString(cartId)).orElse(null);

                        if (cart != null && !cachedCart.equals(cart.getBooks())) {
                            cart.setBooks(cachedCart);
                            cartRepository.save(cart);
                            redisService.reset(versionKey);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during batch synchronization: " + e.getMessage());
        }

        System.out.println("Finishing");
    }
}

