package skytales.Carts.util.redis.util;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import skytales.Carts.model.BookItemReference;
import skytales.Carts.model.Cart;
import skytales.Carts.util.redis.RedisService;
import skytales.Carts.repository.CartRepository;


import java.util.Set;
import java.util.UUID;

@Service
public class RedisMessageSubscriber {


    private final RedisService redisService;
    private final CartRepository cartRepository;

    public RedisMessageSubscriber(RedisService redisService, CartRepository cartRepository) {

        this.redisService = redisService;
        this.cartRepository = cartRepository;
    }

    public void onMessage(String message, String channel) {
        System.out.println("Received message: " + message + " from channel: " + channel);

        String cartVersionPart = message.substring("syncRequest:".length());
        String[] cartIds = cartVersionPart.split(",");

        for (String cartId : cartIds) {
            syncCart(cartId);
        }
    }


    @Async
    protected void syncCart(String cartId) {
        try {
            String versionKey = "cartVersion:" + cartId;

            Cart cart = cartRepository.findById(UUID.fromString(cartId)).orElse(null);

            if (cart != null) {

                Set<BookItemReference> cachedCart = redisService.get("shopping_cart:" + cartId);

                if (cachedCart != null && !cachedCart.equals(cart.getBooks())) {

                    cart.setBooks(cachedCart);
                    cartRepository.save(cart);
                    redisService.reset(versionKey);
                }
            }
        } catch (Exception e) {
            System.err.println("Error synchronizing cart with ID " + cartId + ": " + e.getMessage());
        }
    }
}
