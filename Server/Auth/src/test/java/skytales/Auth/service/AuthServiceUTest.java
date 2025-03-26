package skytales.Auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;
import skytales.Auth.web.dto.LoginRequest;
import skytales.Auth.web.dto.LoginResponse;
import skytales.Auth.web.dto.RegisterRequest;
import skytales.Auth.web.dto.RegisterResponse;
import skytales.Auth.model.Role;
import skytales.Auth.model.User;
import skytales.Auth.repository.UserRepository;


import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceUTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetById() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = authService.getById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Test
    void testIsEmailTaken() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(user);

        User result = authService.isEmailTaken(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void testRegister() {
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", "password123", "password123");

        when(userRepository.findByEmail(registerRequest.email())).thenReturn(null);
        when(bCryptPasswordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(UUID.fromString("a3983b36-6094-4eea-bd37-297f8aee3073"));
            return savedUser;
        });

        AuthService spyUserService = spy(authService);

        doReturn(UUID.randomUUID()).when(spyUserService).createCartForUser(any(UUID.class));
        doNothing().when(spyUserService).assignCart(any(UUID.class), any(UUID.class));

        User result = spyUserService.register(registerRequest, bCryptPasswordEncoder);

        assertNotNull(result);
        assertEquals(registerRequest.email(), result.getEmail());
        assertNotNull(result.getId());

        verify(spyUserService, times(1)).createCartForUser(result.getId());
        verify(spyUserService, times(1)).assignCart(any(UUID.class), eq(result.getId()));
    }

    @Test
    void testLogin() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password");
        User user = new User();
        user.setEmail(loginRequest.email());
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(loginRequest.email())).thenReturn(user);
        when(bCryptPasswordEncoder.matches(loginRequest.password(), user.getPassword())).thenReturn(true);

        User result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals(loginRequest.email(), result.getEmail());
    }

    @Test
    void testGenerateLoginResponse() {
        User user = new User();
        user.setId(UUID.fromString("a3983b36-6094-4eea-bd37-297f8aee3073"));
        user.setRole(Role.USER);
        user.setEmail("test@example.com");
        user.setUsername("testuser");

        when(jwtService.generateToken(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("jwtToken");

        LoginResponse response = authService.generateLoginResponse(user);

        assertNotNull(response);
        assertEquals(user.getId().toString(), response.userId());
        assertEquals(user.getRole().name(), response.role());
        assertEquals("jwtToken", response.jwtToken());
    }

    @Test
    void testGenerateRegisterResponse() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setId(UUID.randomUUID());
        user.setRole(Role.USER);
        user.setUsername("testuser");

        when(jwtService.generateToken(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("jwtToken");

        RegisterResponse response = authService.generateRegisterResponse(user);

        assertNotNull(response);
        assertEquals("test@example.com", response.email());
        assertEquals(user.getId().toString(), response.userId());
        assertEquals(user.getRole().name(), response.role());
        assertEquals("jwtToken", response.jwtToken());
    }

    @Test
    void testCreateToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole(Role.USER);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setCartId(UUID.randomUUID());

        when(jwtService.generateToken(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("jwtToken");

        String token = authService.createToken(user);

        assertNotNull(token);
        assertEquals("jwtToken", token);
    }

    @Test
    void testAssignCart() {
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        authService.assignCart(cartId, userId);

        assertEquals(cartId, user.getCartId());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testCreateCartForUser() {
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("cartId", cartId.toString())));

        UUID result = authService.createCartForUser(userId);

        assertNotNull(result);
        assertEquals(cartId, result);
    }

    @Test
    void testLogin_WrongEmail() {
        LoginRequest loginRequest = new LoginRequest("wrongemail@example.com", "password");

        when(userRepository.findByEmail(loginRequest.email())).thenReturn(null);
        assertThrows(Error.class, () -> authService.login(loginRequest), "wrong email");
    }

    @Test
    void testLogin_WrongPassword() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "wrongpassword");
        User user = new User();
        user.setEmail(loginRequest.email());
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(loginRequest.email())).thenReturn(user);
        when(bCryptPasswordEncoder.matches(loginRequest.password(), user.getPassword())).thenReturn(false);

        assertThrows(Error.class, () -> authService.login(loginRequest), "wrong password");
    }


}