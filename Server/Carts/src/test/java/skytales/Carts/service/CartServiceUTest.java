package skytales.Carts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import skytales.Carts.model.BookItemReference;
import skytales.Carts.model.Cart;
import skytales.Carts.repository.BookItemReferenceRepository;
import skytales.Carts.repository.CartRepository;
import skytales.Carts.util.redis.RedisService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceUTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private BookItemReferenceRepository bookItemReferenceRepository;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void testGetCartByUserId() {
        UUID userId = UUID.randomUUID();
        Cart expectedCart = new Cart();
        when(cartRepository.findCartByOwner(userId)).thenReturn(Optional.of(expectedCart));

        Cart result = cartService.getCartByUserId(userId);

        assertEquals(expectedCart, result);
    }

    @Test
    void testGetByBookId() {
        UUID bookId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();


        BookItemReference expectedBook = new BookItemReference();
        when(bookItemReferenceRepository.findById(bookId)).thenReturn(Optional.of(expectedBook));

        BookItemReference result = cartService.getByBookId(bookId);

        assertEquals(expectedBook, result);
    }

    @Test
    void testAddToCart() {
        UUID cartId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        BookItemReference bookItemReference = new BookItemReference();
        Cart cart = new Cart();
        cart.setBooks(new HashSet<>());

        when(bookItemReferenceRepository.findById(bookId)).thenReturn(Optional.of(bookItemReference));
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        cartService.addToCart(cartId, bookId);

        verify(cartRepository).save(cart);
        verify(redisService).incrBy("cartVersion:" + cartId);
        verify(redisService).set("shopping_cart:" + cartId, cart.getBooks());
    }

    @Test
    void testDeleteFromCart() {
        UUID cartId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        BookItemReference bookItemReference = new BookItemReference();
        Cart cart = new Cart();
        cart.setBooks(new HashSet<>(Collections.singletonList(bookItemReference)));

        when(bookItemReferenceRepository.findById(bookId)).thenReturn(Optional.of(bookItemReference));
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        cartService.deleteFromCart(cartId, bookId);

        verify(cartRepository).save(cart);
        verify(redisService).set("shopping_cart:" + cartId, cart.getBooks());
        verify(redisService).incrBy("cartVersion:" + cartId);
    }

    @Test
    void testGetCartItems() {
        UUID cartId = UUID.randomUUID();
        Cart cart = new Cart();
        Set<BookItemReference> books = new HashSet<>(Arrays.asList(new BookItemReference(), new BookItemReference()));
        cart.setBooks(books);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        Set<BookItemReference> result = cartService.getCartItems(cartId);

        assertEquals(books, result);
        verify(redisService).set("shopping_cart:" + cartId, books);
    }

    @Test
    void testClearCart() {
        UUID userId = UUID.randomUUID();
        Cart cart = new Cart();
        cart.setBooks(new HashSet<>());

        when(cartRepository.findCartByOwner(userId)).thenReturn(Optional.of(cart));

        cartService.clearCart(userId);

        verify(cartRepository).save(cart);
        verify(redisService).delete("shopping_cart:" + cart.getId());
    }

    @Test
    public void testCreateCartForUser_Success() {

       Cart cart = Cart.builder()
                .id(UUID.randomUUID())
                .owner(UUID.randomUUID())
                .books(new HashSet<>())
                .build();

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