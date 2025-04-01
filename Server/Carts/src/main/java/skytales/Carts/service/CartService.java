package skytales.Carts.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;

import skytales.Carts.model.BookItemReference;
import skytales.Carts.model.Cart;
import skytales.Carts.util.redis.RedisService;
import skytales.Carts.repository.BookItemReferenceRepository;
import skytales.Carts.repository.CartRepository;


import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final BookItemReferenceRepository bookItemReferenceRepository;
    private final RedisService redisService;
    private final ObjectMapper jacksonObjectMapper;

    public CartService(CartRepository cartRepository, BookItemReferenceRepository bookItemReferenceRepository, RedisService redisService, ObjectMapper jacksonObjectMapper) {
        this.cartRepository = cartRepository;
        this.bookItemReferenceRepository = bookItemReferenceRepository;
        this.redisService = redisService;
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    public Cart getCartByUserId(UUID id) {
        return cartRepository.findCartByOwner(id)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    public BookItemReference getByBookId(UUID id) {
        return bookItemReferenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

    }

    public void addToCart(UUID cartId, UUID bookId) throws JsonProcessingException {

        BookItemReference bookItemReference = getByBookId(bookId);
        String cartKey = "shopping_cart:" + cartId;
        String versionKey = "cartVersion:" + cartId;

        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            cart.getBooks().add(bookItemReference);
            cartRepository.save(cart);
            redisService.incrBy(versionKey);
            redisService.set(cartKey, cart.getBooks());
            log.info("Cart state saved to cache - add item");

        } catch (RedisConnectionFailureException e) {

            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            cart.getBooks().add(bookItemReference);
            cartRepository.save(cart);

        }
    }

    public void deleteFromCart(UUID cartId, UUID bookId) {

        BookItemReference bookItemReference = getByBookId(bookId);

        String cartKey = "shopping_cart:" + cartId;
        String versionKey = "cartVersion:" + cartId;

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        try {
//            Set<BookItemReference> cachedCart = redisService.get(cartKey);
//
//            if (cachedCart != null) {
//                log.info("Cart state removed WITH redis");
//                cachedCart.remove(bookItemReference);
//                redisService.set(cartKey, cachedCart);
//                redisService.incrBy(versionKey);
//
//            } else {


            cart.getBooks().remove(bookItemReference);
            cartRepository.save(cart);

            redisService.set(cartKey, cart.getBooks());
            redisService.incrBy(versionKey);
            log.info("Cart state saved to cache - remove item");

        } catch (RedisConnectionFailureException e) {


            cart.getBooks().remove(bookItemReference);
            log.info("Cache unavailable, saving only to disk - add item");

            cartRepository.save(cart);

        }
    }

    public Set<BookItemReference> getCartItems(UUID cartId) {

        String cartKey = "shopping_cart:" + cartId;


        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        try {

            redisService.set(cartKey, cart.getBooks());
            log.info("Cart state saved to cache - get item");

            return cart.getBooks();
        } catch (RedisConnectionFailureException e) {

            log.info("Cache unavailable, saving only to disk - remove item");
            return cart.getBooks();
        }
    }

    public void clearCart(UUID id) {

        Cart cart = getCartByUserId(id);
        cart.getBooks().clear();
        cartRepository.save(cart);

        try {
            String cartKey = "shopping_cart:" + cart.getId();
            redisService.delete(cartKey);
        } catch (RedisConnectionFailureException _) {
            log.info("An error occurred while clearing the cart from cache, redis is currently unavailable.");
        }

        System.out.println(cart.getBooks());
    }

    public String createCartForUser(String userId) {
        UUID identityForUser = UUID.fromString(userId);

        Cart cart = Cart.builder()
                .owner(identityForUser)
                .build();

        cartRepository.save(cart);
        log.info("Cart created for user with id -" + userId);

        return cart.getId().toString();
    }
}





