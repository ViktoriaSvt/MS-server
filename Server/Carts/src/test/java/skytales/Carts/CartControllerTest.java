package skytales.Carts;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import skytales.Carts.config.TestConfig;
import skytales.Carts.repository.BookItemReferenceRepository;
import skytales.Carts.repository.CartRepository;
import skytales.Carts.service.CartService;
import skytales.Carts.util.config.security.JwtAuthenticationFilter;
import skytales.Carts.util.config.security.SecurityConfig;
import skytales.Carts.web.CartController;


import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Import({SecurityConfig.class, TestConfig.class})
@ExtendWith(MockitoExtension.class)
@WebMvcTest(CartController.class)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private CartRepository cartRepository;

    @MockitoBean
    private BookItemReferenceRepository bookItemReferenceRepository;

    @InjectMocks
    private CartController cartController;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private String token;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("73fded46-c09b-49cf-b581-8ed145a887fe");
        token = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsImNhcnRJZCI6IjYzMDQ2OThkLTlmMzQtNDQ4ZC1iM2VlLWRjNzQ1ZWNkYmJiNSIsInVzZXJJZCI6IjI2YTAwMTY1LWRhYmUtNDgyYS1iOGE1LThiMDlmNTY1NGQyNSIsImVtYWlsIjoidGVzdGVtYWlsQGFidi5iZyIsInVzZXJuYW1lIjoidXNlcm5hbWUiLCJzdWIiOiJ0ZXN0ZW1haWxAYWJ2LmJnIiwiaWF0IjoxNzQzMTg1NTU0LCJleHAiOjE3NDMxODgxNDZ9.RzlQIC9pOvI0bGVcSiSfCsV6C_HcRQDoeowJo0F2dUk";

    }

    @Test
    void testAddToCart() throws Exception {
        doNothing().when(cartService).addToCart(any(UUID.class), any(UUID.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/cart/add/{id}", "4aef79dc-8535-44df-8fcb-769891d0f91e")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("cartId", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }


    @Test
    void testRemoveFromCart() throws Exception {
        doNothing().when(cartService).deleteFromCart(any(UUID.class), any(UUID.class));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/cart/remove/{id}", "4aef79dc-8535-44df-8fcb-769891d0f91e")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetItems() throws Exception {
        when(cartService.getCartItems(any(UUID.class))).thenReturn(Collections.emptySet());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateCart() throws Exception {
        when(cartService.createCartForUser(any(String.class))).thenReturn("cart456");

        Map<String, String> requestBody = Collections.singletonMap("userId", "123e4567-e89b-12d3-a456-426614174000");
        String jsonRequestBody = objectMapper.writeValueAsString(requestBody);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/cart/createCart")
                        .header("Authorization", "Bearer " + token)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
