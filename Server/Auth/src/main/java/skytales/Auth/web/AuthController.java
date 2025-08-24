package skytales.Auth.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import skytales.Auth.service.AuthService;
import skytales.Auth.service.JwtService;
import skytales.Auth.service.UserService;
import skytales.Auth.web.dto.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/auth")
public class AuthController {


    private final UserService userService;
    private final AuthService authService;
    private final JwtService jwtService;


    public AuthController(UserService userService, AuthService authService, JwtService jwtService) {
        this.userService = userService;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest registerRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }

        RegisterResponse registerResponse = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }

        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }

    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpServletRequest request) {
        SessionResponse sessionResponse = authService.getSession(request);
        return ResponseEntity.ok(sessionResponse);
    }


}
