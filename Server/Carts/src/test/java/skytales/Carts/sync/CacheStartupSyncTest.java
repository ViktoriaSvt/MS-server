package skytales.Carts.sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import skytales.Carts.model.BookItemReference;
import skytales.Carts.model.Cart;
import skytales.Carts.repository.CartRepository;
import skytales.Carts.util.redis.RedisService;
import skytales.Carts.util.redis.sync.CacheStartupSync;


import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheStartupSyncTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private CacheStartupSync cacheStartupSync;

    private Cart cart;
    private Set<BookItemReference> cartBooks;
    private String cartKey;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setId(UUID.randomUUID());
        cartBooks = Collections.singleton(new BookItemReference());
        cart.setBooks(cartBooks);
        cartKey = "shopping_cart:" + cart.getId();
    }

    @Test
    void syncCache_Success() {
        when(cartRepository.findAll()).thenReturn(Collections.singletonList(cart));
        when(redisService.get(cartKey)).thenReturn(null);

        cacheStartupSync.syncCache();

        verify(redisService, times(1)).set(cartKey, cartBooks);
    }

    @Test
    void syncCache_CachedCartEqualsCartBooks() {
        when(cartRepository.findAll()).thenReturn(Collections.singletonList(cart));
        when(redisService.get(cartKey)).thenReturn(cartBooks);

        cacheStartupSync.syncCache();

        verify(redisService, never()).set(anyString(), anySet());
    }

    @Test
    void syncCache_CachedCartNotEqualsCartBooks() {
        when(cartRepository.findAll()).thenReturn(Collections.singletonList(cart));
        when(redisService.get(cartKey)).thenReturn(Collections.emptySet());

        cacheStartupSync.syncCache();

        verify(redisService, times(1)).set(cartKey, cartBooks);
    }

    @Test
    void syncCache_Exception() {
        when(cartRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        cacheStartupSync.syncCache();

        verify(redisService, never()).set(anyString(), anySet());
    }
}