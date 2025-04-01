package skytales.Auth.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import org.springframework.web.client.RestTemplate;
import skytales.Auth.util.config.security.JwtAuthenticationFilter;
import skytales.Auth.util.config.security.SecurityConfig;
import skytales.Auth.service.AuthService;
import skytales.Auth.web.dto.SessionResponse;
import skytales.Auth.model.User;
import skytales.Auth.repository.UserRepository;
import skytales.Auth.service.JwtService;
import skytales.Auth.service.UserService;
import skytales.Auth.web.UserController;


import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

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
    private UserController userController;

    private User user;
    private UUID userId;
    private String token;
    private SessionResponse mockSessionResponse;

    @BeforeEach
    void setUp() {
        token = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsImNhcnRJZCI6IjYzMDQ2OThkLTlmMzQtNDQ4ZC1iM2VlLWRjNzQ1ZWNkYmJiNSIsInVzZXJJZCI6IjI2YTAwMTY1LWRhYmUtNDgyYS1iOGE1LThiMDlmNTY1NGQyNSIsImVtYWlsIjoidGVzdGVtYWlsQGFidi5iZyIsInVzZXJuYW1lIjoidXNlcm5hbWUiLCJzdWIiOiJ0ZXN0ZW1haWxAYWJ2LmJnIiwiaWF0IjoxNzQzMTg1NTU0LCJleHAiOjE3NDMxODgxNDZ9.RzlQIC9pOvI0bGVcSiSfCsV6C_HcRQDoeowJo0F2dUk";

        userId = UUID.fromString("73fded46-c09b-49cf-b581-8ed145a887fe");
        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setUsername("testuser");

        mockSessionResponse = new SessionResponse("user1@example.com", "user1", "123e4567-e89b-12d3-a456-426614174000", "USER", "cart456");
    }

    @Test
    void testGetUser_Success() throws Exception {
        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testGetUser_NotFound() throws Exception {

        when(userService.getById(any(UUID.class))).thenThrow(new NoSuchElementException("User was not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk());
    }

}