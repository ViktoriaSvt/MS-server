package skytales.Carts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.data.redis.RedisConnectionFailureException;
import skytales.Carts.model.BookItemReference;
import skytales.Carts.model.Cart;
import skytales.Carts.repository.BookItemReferenceRepository;
import skytales.Carts.repository.CartRepository;
import skytales.Carts.util.redis.RedisService;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private BookItemReferenceRepository bookItemReferenceRepository;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private CartService cartService;

    private Cart cart;
    private BookItemReference bookItemReference;

    @BeforeEach
    void setUp() {
        UUID cartId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        cart = Cart.builder()
                .id(cartId)
                .owner(UUID.randomUUID())
                .books(new HashSet<>())
                .build();



        bookItemReference = new BookItemReference();
        bookItemReference.setBookId(bookId);

        lenient().when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        lenient().when(bookItemReferenceRepository.findById(bookId)).thenReturn(Optional.of(bookItemReference));
    }

    @Test
    void testAddToCartWithRedis() {
        UUID cartId = cart.getId();
        UUID bookId = bookItemReference.getBookId();

        Set<BookItemReference> cachedCart = new HashSet<>();
        cachedCart.add(bookItemReference);
        when(redisService.get("shopping_cart:" + cartId)).thenReturn(cachedCart);

        cartService.addToCart(cartId, bookId);

        verify(redisService, times(1)).get("shopping_cart:" + cartId);
        verify(redisService, times(1)).set("shopping_cart:" + cartId, cachedCart);
    }

    @Test
    void testAddToCartWithoutRedis() {
        UUID cartId = cart.getId();
        UUID bookId = bookItemReference.getBookId();

        when(redisService.get("shopping_cart:" + cartId)).thenReturn(null);

        cartService.addToCart(cartId, bookId);

        verify(redisService, times(1)).get("shopping_cart:" + cartId);
        verify(cartRepository, times(1)).save(cart);
        verify(redisService, times(1)).set("shopping_cart:" + cartId, cart.getBooks());
    }

    @Test
    void testGetCartItemsFromRedis() {
        UUID cartId = cart.getId();

        Set<BookItemReference> cachedCart = new HashSet<>();
        cachedCart.add(bookItemReference);
        when(redisService.get("shopping_cart:" + cartId)).thenReturn(cachedCart);

        Set<BookItemReference> cartItems = cartService.getCartItems(cartId);

        assertEquals(1, cartItems.size());
        verify(redisService, times(1)).get("shopping_cart:" + cartId);
    }

    @Test
    void testGetCartItemsWithoutRedis() {
        UUID cartId = cart.getId();

        when(redisService.get("shopping_cart:" + cartId)).thenReturn(null);

        Set<BookItemReference> cartItems = cartService.getCartItems(cartId);

        assertEquals(cart.getBooks().size(), cartItems.size());
        verify(cartRepository, times(1)).findById(cartId);
        verify(redisService, times(1)).set("shopping_cart:" + cartId, cart.getBooks());
    }

    @Test
    void testDeleteFromCartWithRedis() {
        UUID cartId = cart.getId();
        UUID bookId = bookItemReference.getBookId();

        Set<BookItemReference> cachedCart = new HashSet<>();
        cachedCart.add(bookItemReference);
        when(redisService.get("shopping_cart:" + cartId)).thenReturn(cachedCart);

        cartService.deleteFromCart(cartId, bookId);

        verify(redisService, times(1)).get("shopping_cart:" + cartId);
        verify(redisService, times(1)).set("shopping_cart:" + cartId, cachedCart);
    }

    @Test
    void testDeleteFromCartWithoutRedis() {
        UUID cartId = cart.getId();
        UUID bookId = bookItemReference.getBookId();

        when(redisService.get("shopping_cart:" + cartId)).thenReturn(null);

        cartService.deleteFromCart(cartId, bookId);

        verify(cartRepository, times(1)).save(cart);
        verify(redisService, times(1)).set("shopping_cart:" + cartId, cart.getBooks());
    }

    @Test
    public void testClearCart_Success() {
        when(cartRepository.findCartByOwner(cart.getOwner())).thenReturn(Optional.ofNullable(cart));

        cartService.clearCart(cart.getOwner());

        verify(cartRepository, times(1)).save(cart);
        verify(redisService, times(1)).delete("shopping_cart:" + cart.getId());
        assertTrue(cart.getBooks().isEmpty());
    }

    @Test
    public void testClearCart_RedisConnectionFailure() {
        when(cartRepository.findCartByOwner(cart.getOwner())).thenReturn(Optional.ofNullable(cart));
        doThrow(new RedisConnectionFailureException("Redis connection failed")).when(redisService).delete(anyString());

        cartService.clearCart(cart.getOwner());

        verify(cartRepository, times(1)).save(cart);
        verify(redisService, times(1)).delete("shopping_cart:" + cart.getId());
        assertTrue(cart.getBooks().isEmpty());
    }

    @Test
    public void testCreateCartForUser_Success() {

        when(cartRepository.save(any(Cart.class))).thenAnswer(new Answer<Cart>() {
            @Override
            public Cart answer(InvocationOnMock invocation) throws Throwable {
                Cart cart = invocation.getArgument(0);
                cart.setId(UUID.randomUUID());
                return cart;
            }
        });

        String cartId = cartService.createCartForUser(String.valueOf(UUID.randomUUID()));

        verify(cartRepository, times(1)).save(any(Cart.class));
        assertNotNull(cart.getId());
    }
}
