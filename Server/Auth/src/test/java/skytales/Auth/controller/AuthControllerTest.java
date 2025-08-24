package skytales.Auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import skytales.Auth.service.AuthService;
import skytales.Auth.util.config.security.SecurityConfig;
import skytales.Auth.web.AuthController;
import skytales.Auth.web.dto.*;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@Import(SecurityConfig.class)
@ExtendWith(MockitoExtension.class)
@WebMvcTest(AuthController.class)
public class AuthControllerTest {


    @MockitoBean private RestTemplate restTemplate;
    @MockitoBean private AuthService authService;

    @Autowired private MockMvc mockMvc;

    @InjectMocks private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testRegister() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("testuser", "test@example.com", "password");
        RegisterResponse registerResponse = new RegisterResponse(
                UUID.randomUUID().toString(),
                "testuser",
                "test@example.com",
                "token"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(registerResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        LoginResponse loginResponse = new LoginResponse(
                UUID.randomUUID().toString(),
                "testuser",
                "test@example.com",
                "token"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testGetSession() throws Exception {
        mockMvc.perform(get("/api/auth/session")
                        .requestAttr("userId", "123e4567-e89b-12d3-a456-426614174000")
                        .requestAttr("username", "testuser")
                        .requestAttr("email", "test@example.com")
                        .requestAttr("role", "admin")
                        .requestAttr("cartId", "cart123"))
                .andExpect(status().isOk());
    }
}
