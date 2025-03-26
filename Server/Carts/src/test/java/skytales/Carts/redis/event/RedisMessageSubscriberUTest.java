package skytales.Carts.redis.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import skytales.Carts.model.BookItemReference;
import skytales.Carts.model.Cart;
import skytales.Carts.repository.CartRepository;
import skytales.Carts.util.redis.RedisService;
import skytales.Carts.util.redis.util.RedisMessageSubscriber;


import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

class RedisMessageSubscriberUTest {



    @Mock
    private RedisService redisService;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private RedisMessageSubscriber redisMessageSubscriber;

    private UUID cartId;
    private Cart cart;
    private Set<BookItemReference> cachedBooks;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cartId = UUID.randomUUID();
        cart = new Cart();
        cart.setId(cartId);
        cart.setBooks(new HashSet<>());
        cachedBooks = Set.of(new BookItemReference());
    }

    @Test
    void testOnMessage_SingleCart_Success() {

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(redisService.get("shopping_cart:" + cartId)).thenReturn(cachedBooks);

        redisMessageSubscriber.onMessage("syncRequest:" + cartId, "syncChannel");

        verify(cartRepository, times(1)).save(cart);
        verify(redisService, times(1)).reset("cartVersion:" + cartId);
    }

    @Test
    void testOnMessage_CartNotFound() {

        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        redisMessageSubscriber.onMessage("syncRequest:" + cartId, "syncChannel");


        verify(cartRepository, never()).save(any(Cart.class));
        verify(redisService, never()).reset("cartVersion:" + cartId);
    }

    @Test
    void testOnMessage_CartIsUpToDate() {

        Set<BookItemReference> booksInDB = Set.of(new BookItemReference());
        cart.setBooks(booksInDB);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(redisService.get("shopping_cart:" + cartId)).thenReturn(booksInDB);

        redisMessageSubscriber.onMessage("syncRequest:" + cartId, "syncChannel");

        verify(cartRepository, never()).save(any(Cart.class));
        verify(redisService, never()).reset("cartVersion:" + cartId);
    }

    @Test
    void testSyncCart_ErrorHandling() {

        when(cartRepository.findById(cartId)).thenThrow(new RuntimeException("DB error"));

        redisMessageSubscriber.onMessage("syncRequest:" + cartId, "syncChannel");

        verify(cartRepository, never()).save(any(Cart.class));
        verify(redisService, never()).reset("cartVersion:" + cartId);
    }
}
