package skytales.Auth.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import skytales.Auth.model.User;
import skytales.Auth.service.AuthService;
import skytales.Auth.service.JwtService;
import skytales.Auth.service.UserService;
import skytales.Auth.web.dto.*;

@Slf4j
@RestController
@RequestMapping("api/auth")
public class AuthController {


    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final JwtService jwtService;


    public AuthController(UserService userService, BCryptPasswordEncoder passwordEncoder, AuthService authService, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }

        User user = authService.register(registerRequest, passwordEncoder);
        RegisterResponse registerResponse = authService.generateRegisterResponse(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }

        User user = authService.login(loginRequest);
        LoginResponse loginResponse = authService.generateLoginResponse(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }


    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpServletRequest request) {

        try {

            String userId = (String) request.getAttribute("userId");
            String username = (String) request.getAttribute("username");
            String email = (String) request.getAttribute("email");
            String role = (String) request.getAttribute("role");
            String cartId = (String) request.getAttribute("cartId");


            SessionResponse sessionResponse = new SessionResponse(email, username, userId, role, cartId);
            return ResponseEntity.ok(sessionResponse);

        } catch (SessionAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


}
