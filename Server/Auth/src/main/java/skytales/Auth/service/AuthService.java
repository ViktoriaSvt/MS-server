package skytales.Auth.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import skytales.Auth.model.Role;
import skytales.Auth.model.User;
import skytales.Auth.repository.UserRepository;
import skytales.Auth.web.dto.LoginRequest;
import skytales.Auth.web.dto.LoginResponse;
import skytales.Auth.web.dto.RegisterRequest;
import skytales.Auth.web.dto.RegisterResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    public AuthService(UserRepository userRepository, JwtService jwtService, RestTemplate restTemplate, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.restTemplate = restTemplate;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public User register(RegisterRequest registerRequest, BCryptPasswordEncoder bCryptPasswordEncoder) {

        User exists = isEmailTaken(registerRequest.email());

        if (exists != null) {
            throw new Error("user already exists!");
        }

        User user = User.builder()
                .role(Role.USER)
                .status("regular")
                .description("No description to show yet...")
                .username(generateRandomUsername())
                .profilePicture("https://icon-library.com/images/user-icon-jpg/user-icon-jpg-28.jpg")
                .email(registerRequest.email())
                .password(bCryptPasswordEncoder.encode(registerRequest.password()))
                .build();

        userRepository.save(user);

        UUID cartId = createCartForUser(user.getId());
        assignCart(cartId, user.getId());

        return user;

    }

    public User login(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.email());
        if (user == null) {
            throw new Error("wrong email");
        }

        if (!bCryptPasswordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new Error("wrong password");
        }
        return user;
    }

    private String generateRandomUsername() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public LoginResponse generateLoginResponse(User user) {

        String jwtToken = createToken(user);

        return new LoginResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name(),
                jwtToken
        );
    }

    public RegisterResponse generateRegisterResponse(User user) {

        System.out.println("Started token token");
        String jwtToken = createToken(user);

        return new RegisterResponse(
                user.getEmail(),
                user.getId().toString(),
                user.getRole().name(),
                jwtToken
        );
    }

    public String createTestToken() {

        String role = "admin";
        String email = "test@example.com";
        String username = "testuser";
        String cartId = String.valueOf(UUID.randomUUID());

      return jwtService.generateToken("a3983b36-6094-4eea-bd37-297f8aee3073",role,email,username,cartId);
    }

    public void assignCart(UUID cartId, UUID userId) {
        User user = getById(userId);
        user.setCartId(cartId);
        userRepository.save(user);
    }

    public UUID createCartForUser(UUID userId) {
        String url = "http://carts:8086/api/cart/createCart";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userId", userId.toString());

        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
        String cartIdStr = (String) response.getBody().get("cartId");

        UUID cartId = UUID.fromString(cartIdStr);

        return cartId;
    }

    public String createToken(User user) {

        return jwtService.generateToken(
                user.getId().toString(),
                user.getRole().name(),
                user.getEmail(),
                user.getUsername(),
                String.valueOf(user.getCartId())
        );
    }

    public User getById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    public User isEmailTaken(String email) {
        return userRepository.findByEmail(email);
    }

}
