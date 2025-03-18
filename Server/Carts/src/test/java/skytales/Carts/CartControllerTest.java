package skytales.Carts;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import skytales.Carts.web.CartController;

import skytales.common.configuration.SecurityConfig;
import skytales.common.security.JwtAuthenticationFilter;
import skytales.common.security.SessionResponse;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
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
    private SessionResponse mockSessionResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("73fded46-c09b-49cf-b581-8ed145a887fe");
        token = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiYWRtaW4iLCJjYXJ0SWQiOiJlZjZlMjk4Ny03ZGU0LTQ1NzAtYTBhYS01MDgwZDRhNDdmYTEiLCJ1c2VySWQiOiJhMzk4M2IzNi02MDk0LTRlZWEtYmQzNy0yOTdmOGFlZTMwNzMiLCJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJ1c2VybmFtZSI6InRlc3R1c2VyIiwic3ViIjoidGVzdEBleGFtcGxlLmNvbSIsImlhdCI6MTc0MjIzNDkyNiwiZXhwIjoxNzQyMzIxMzI2fQ.1nJBH-ei2BCs7HOUJmCnu1-wbQhRfij2qfbBYbTZFok";

        mockSessionResponse = new SessionResponse("user1@example.com", "user1", "123e4567-e89b-12d3-a456-426614174000", "USER", UUID.randomUUID().toString());
    }

    @Test
    void testAddToCart() throws Exception {
        doNothing().when(cartService).addToCart(any(UUID.class), any(UUID.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/cart/add/{id}", "4aef79dc-8535-44df-8fcb-769891d0f91e")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testRemoveFromCart() throws Exception {
        doNothing().when(cartService).deleteFromCart(any(UUID.class), any(UUID.class));

        mockMvc.perform(MockMvcRequestBuilders.delete("/cart/remove/{id}", "4aef79dc-8535-44df-8fcb-769891d0f91e")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetItems() throws Exception {
        when(cartService.getCartItems(any(UUID.class))).thenReturn(Collections.emptySet());

        mockMvc.perform(MockMvcRequestBuilders.get("/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateCart() throws Exception {
        when(cartService.createCartForUser(any(String.class))).thenReturn("cart456");

        Map<String, String> requestBody = Collections.singletonMap("userId", "123e4567-e89b-12d3-a456-426614174000");
        String jsonRequestBody = objectMapper.writeValueAsString(requestBody);

        mockMvc.perform(MockMvcRequestBuilders.post("/cart/createCart")
                        .header("Authorization", "Bearer " + token)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
