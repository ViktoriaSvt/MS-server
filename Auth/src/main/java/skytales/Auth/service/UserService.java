package skytales.auth.service;


import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import skytales.auth.dto.*;
import skytales.auth.model.Role;
import skytales.auth.model.User;
import skytales.auth.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, JwtService jwtService, RestTemplate restTemplate, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.restTemplate = restTemplate;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public List<UserListItem> listUsers() {
        List<User> users = userRepository.findAll();
        return convertUsersToUserListRequest(users);
    }

    public List<UserListItem> convertUsersToUserListRequest(List<User> users) {
        return users.stream()
                .map(user -> new UserListItem(user.getUsername(), user.getEmail(), user.getRole().toString()))
                .collect(Collectors.toList());
    }

    public User getById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    public User isEmailTaken(String email) {
        return userRepository.findByEmail(email);
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
                user.getUsername(),
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

    public String createToken(User user) {

        return jwtService.generateToken(
                user.getId().toString(),
                user.getRole().name(),
                user.getEmail(),
                user.getUsername(),
                String.valueOf(user.getCartId())
        );
    }

    public void assignCart(UUID cartId, UUID userId) {
        User user = getById(userId);
        user.setCartId(cartId);
        userRepository.save(user);
    }

    public UUID createCartForUser(UUID userId) {
        String url = "http://localhost:8080/cart/createCart";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userId", userId.toString());

        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
        String cartIdStr = (String) response.getBody().get("cartId");

        UUID cartId = UUID.fromString(cartIdStr);

        return cartId;
    }
}
