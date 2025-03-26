package skytales.Carts.sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import skytales.Carts.model.BookItemReference;
import skytales.Carts.model.Cart;
import skytales.Carts.repository.CartRepository;
import skytales.Carts.util.redis.RedisService;
import skytales.Carts.util.redis.sync.CartBatchSync;


import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartBatchSyncUTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private CartBatchSync cartBatchSync;

    private String cartKey;
    private Set<BookItemReference> cachedCart;
    private Cart cart;
    private UUID cartId;

    @BeforeEach
    void setUp() {
        cartId = UUID.randomUUID();
        cartKey = "shopping_cart:" + cartId;
        cachedCart = Collections.singleton(new BookItemReference());
        cart = new Cart();
        cart.setId(cartId);
        cart.setBooks(Collections.emptySet());
    }

    @Test
    void syncCartsBatch_Success() {
        when(redisTemplate.keys("shopping_cart:*")).thenReturn(Collections.singleton(cartKey));
        when(redisService.get(cartKey)).thenReturn(cachedCart);
        when(redisService.getTerm("cartVersion:" + cartId)).thenReturn(1);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        cartBatchSync.syncCartsBatch();

        verify(cartRepository, times(1)).save(cart);
        verify(redisService, times(1)).reset("cartVersion:" + cartId);
    }

    @Test
    void syncCartsBatch_TermZero() {
        when(redisTemplate.keys("shopping_cart:*")).thenReturn(Collections.singleton(cartKey));
        when(redisService.get(cartKey)).thenReturn(cachedCart);
        when(redisService.getTerm("cartVersion:" + cartId)).thenReturn(0);

        cartBatchSync.syncCartsBatch();

        verify(cartRepository, never()).save(any(Cart.class));
        verify(redisService, never()).reset(anyString());
    }

    @Test
    void syncCartsBatch_CartNotFound() {
        when(redisTemplate.keys("shopping_cart:*")).thenReturn(Collections.singleton(cartKey));
        when(redisService.get(cartKey)).thenReturn(cachedCart);
        when(redisService.getTerm("cartVersion:" + cartId)).thenReturn(1);
        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        cartBatchSync.syncCartsBatch();

        verify(cartRepository, never()).save(any(Cart.class));
        verify(redisService, never()).reset(anyString());
    }

    @Test
    void syncCartsBatch_CachedCartNull() {
        when(redisTemplate.keys("shopping_cart:*")).thenReturn(Collections.singleton(cartKey));
        when(redisService.get(cartKey)).thenReturn(null);

        cartBatchSync.syncCartsBatch();

        verify(cartRepository, never()).save(any(Cart.class));
        verify(redisService, never()).reset(anyString());
    }

    @Test
    void syncCartsBatch_NoCartKeys() {
        when(redisTemplate.keys("shopping_cart:*")).thenReturn(null);

        cartBatchSync.syncCartsBatch();

        verify(cartRepository, never()).save(any(Cart.class));
        verify(redisService, never()).reset(anyString());
    }

    @Test
    void testSyncCartsBatch_ExceptionHandling() {
        CartBatchSync spyService = spy(cartBatchSync);
        doThrow(new RuntimeException("Redis execution failed")).when(redisTemplate).keys("shopping_cart:*");

        spyService.syncCartsBatch();

        verify(spyService).syncCartsBatch();
    }
}