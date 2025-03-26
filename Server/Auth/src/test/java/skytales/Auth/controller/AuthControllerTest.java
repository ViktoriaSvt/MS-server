package skytales.Auth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import skytales.Auth.util.config.security.JwtAuthenticationFilter;
import skytales.Auth.util.config.security.SecurityConfig;
import skytales.Auth.model.User;
import skytales.Auth.repository.UserRepository;
import skytales.Auth.service.AuthService;
import skytales.Auth.service.JwtService;
import skytales.Auth.service.UserService;
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

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();
    private String token;

    @BeforeEach
    public void setUp() throws Exception {
        token = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiYWRtaW4iLCJjYXJ0SWQiOiIzYzExYzNlNi1hNzllLTQ2N2EtYWJhZi0yOGQ0OGQxZjdiM2IiLCJ1c2VySWQiOiJhMzk4M2IzNi02MDk0LTRlZWEtYmQzNy0yOTdmOGFlZTMwNzMiLCJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJ1c2VybmFtZSI6InRlc3R1c2VyIiwic3ViIjoidGVzdEBleGFtcGxlLmNvbSIsImlhdCI6MTc0MjczMjUzMSwiZXhwIjoxNzQyNzM3Nzg3fQ.eNG2LyyvCpR8DPFE6rEFWi3vUFoi5pdmXtOa8rzNOgs";
    }

    @Test
    void testRegister() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("testuser", "test@example.com", "password");
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        RegisterResponse registerResponse = new RegisterResponse(user.getId().toString(), user.getUsername(), user.getEmail(), "token");

        when(authService.register(any(RegisterRequest.class), any(BCryptPasswordEncoder.class))).thenReturn(user);
        when(authService.generateRegisterResponse(any(User.class))).thenReturn(registerResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        LoginResponse loginResponse = new LoginResponse(user.getId().toString(), user.getUsername(), user.getEmail(), "token");

        when(authService.login(any(LoginRequest.class))).thenReturn(user);
        when(authService.generateLoginResponse(any(User.class))).thenReturn(loginResponse);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("Response JSON: " + response);
    }

    @Test
    void testGetSession() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("userId")).thenReturn("123e4567-e89b-12d3-a456-426614174000");
        when(request.getAttribute("username")).thenReturn("testuser");
        when(request.getAttribute("email")).thenReturn("test@example.com");
        when(request.getAttribute("role")).thenReturn("admin");
        when(request.getAttribute("cartId")).thenReturn("cart123");

        mockMvc.perform(get("/api/auth/session")
                        .header("Authorization", "Bearer " + token)
                        .requestAttr("userId", "123e4567-e89b-12d3-a456-426614174000")
                        .requestAttr("username", "testuser")
                        .requestAttr("email", "test@example.com")
                        .requestAttr("role", "admin")
                        .requestAttr("cartId", "cart123"))
                .andExpect(status().isOk());

    }
}