package skytales.Auth;


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

import skytales.Auth.web.dto.SessionResponse;
import skytales.Auth.model.User;
import skytales.Auth.repository.UserRepository;
import skytales.Auth.service.JwtService;
import skytales.Auth.service.UserService;
import skytales.Auth.web.UserController;
import skytales.common.configuration.SecurityConfig;
import skytales.common.security.JwtAuthenticationFilter;
import skytales.common.security.SessionService;

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
    private UserRepository userRepository;

    @MockitoBean
    private SessionService sessionService;

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
        token = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiYWRtaW4iLCJjYXJ0SWQiOiJlZjZlMjk4Ny03ZGU0LTQ1NzAtYTBhYS01MDgwZDRhNDdmYTEiLCJ1c2VySWQiOiJhMzk4M2IzNi02MDk0LTRlZWEtYmQzNy0yOTdmOGFlZTMwNzMiLCJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJ1c2VybmFtZSI6InRlc3R1c2VyIiwic3ViIjoidGVzdEBleGFtcGxlLmNvbSIsImlhdCI6MTc0MjIzNDkyNiwiZXhwIjoxNzQyMzIxMzI2fQ.1nJBH-ei2BCs7HOUJmCnu1-wbQhRfij2qfbBYbTZFok";

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
        when(userService.getById(any(UUID.class))).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andDo(print())  // Log the response body to console
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }
}