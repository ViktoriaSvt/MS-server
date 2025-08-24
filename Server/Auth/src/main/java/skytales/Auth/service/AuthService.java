package skytales.Auth.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import skytales.Auth.model.Role;
import skytales.Auth.model.User;
import skytales.Auth.repository.UserRepository;
import skytales.Auth.util.exceptions.InvalidCredentialsException;
import skytales.Auth.util.exceptions.UserAlreadyExistsException;
import skytales.Auth.util.exceptions.UserNotFoundException;
import skytales.Auth.web.dto.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new UserAlreadyExistsException("User with this email already exists!");
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

        return generateRegisterResponse(user);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!bCryptPasswordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return generateLoginResponse(user);
    }

    public SessionResponse getSession(HttpServletRequest request) {
        String userId = Objects.requireNonNull((String) request.getAttribute("userId"), "userId is missing");
        String username = Objects.requireNonNull((String) request.getAttribute("username"), "username is missing");
        String email = Objects.requireNonNull((String) request.getAttribute("email"), "email is missing");
        String role = Objects.requireNonNull((String) request.getAttribute("role"), "role is missing");
        String cartId = Objects.requireNonNull((String) request.getAttribute("cartId"), "cartId is missing");

        return new SessionResponse(email, username, userId, role, cartId);
    }

    public void assignCart(UUID cartId, UUID userId) {
        User user = getById(userId);
        user.setCartId(cartId);
        userRepository.save(user);
    }

    //Temporary value: "http://carts:8086/api/cart/createCart";
    public UUID createCartForUser(UUID userId) {
        String url = "http://localhost:8086/api/cart/createCart";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userId", userId.toString());

        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
        String cartIdStr = (String) response.getBody().get("cartId");

        return UUID.fromString(cartIdStr);
    }

    public User getById(UUID id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        return user;
    }

    private String generateRandomUsername() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private LoginResponse generateLoginResponse(User user) {
        String jwtToken = createToken(user);
        return new LoginResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name(),
                jwtToken
        );
    }

    private RegisterResponse generateRegisterResponse(User user) {
        String jwtToken = createToken(user);
        return new RegisterResponse(
                user.getEmail(),
                user.getId().toString(),
                user.getRole().name(),
                jwtToken
        );
    }

    private String createToken(User user) {
        return jwtService.generateToken(
                user.getId().toString(),
                user.getRole().name(),
                user.getEmail(),
                user.getUsername(),
                String.valueOf(user.getCartId())
        );
    }
}
