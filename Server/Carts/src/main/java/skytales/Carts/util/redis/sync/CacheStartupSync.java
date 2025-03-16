package skytales.Carts.util.redis.sync;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.stereotype.Service;
import skytales.Carts.model.BookItemReference;
import skytales.Carts.model.Cart;
import skytales.Carts.util.redis.RedisService;
import skytales.Carts.repository.CartRepository;


import java.util.List;
import java.util.Set;

@Service
@EnableKafka
public class CacheStartupSync {

    private final CartRepository cartRepository;
    private final RedisService redisService;

    @Autowired
    public  CacheStartupSync(CartRepository cartRepository, RedisService redisService) {

        this.cartRepository = cartRepository;
        this.redisService = redisService;
    }

    public void syncCache() {
        System.out.println("Retrieving state from database to update Redis...");

        try {
            List<Cart> allCarts = cartRepository.findAll();

            for (Cart cart : allCarts) {
                String cartKey = "shopping_cart:" + cart.getId();
                Set<BookItemReference> cartBooks = cart.getBooks();

                Set<BookItemReference> cachedCart = redisService.get(cartKey);

                if (cachedCart == null || !cachedCart.equals(cartBooks)) {
                    redisService.set(cartKey, cartBooks);
                }
            }
        } catch (Exception e) {
            System.err.println("Error during batch synchronization: " + e.getMessage());
        }

        System.out.println("Batch synchronization finished.");
    }
}
