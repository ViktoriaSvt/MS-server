package AuthRoot;

import AuthRoot.dto.LoginRequest;
import AuthRoot.dto.LoginResponse;
import AuthRoot.dto.RegisterRequest;
import AuthRoot.dto.RegisterResponse;
import AuthRoot.model.User;
import AuthRoot.service.UserService;
import com.skytales.common.common_utils.common.security.SessionResponse;
import com.skytales.common.common_utils.common.security.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {


    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    public AuthController(UserService userService, BCryptPasswordEncoder passwordEncoder, SessionService sessionService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }

        User user = userService.register(registerRequest, passwordEncoder);
        RegisterResponse registerResponse = userService.generateRegisterResponse(user);
        System.out.println("finished token");
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }

        User user = userService.login(loginRequest);
        LoginResponse loginResponse = userService.generateLoginResponse(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }


    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpServletRequest request) {

        try {
            SessionResponse sessionResponse = sessionService.getSessionData(request);
            return ResponseEntity.ok(sessionResponse);

        } catch (SessionAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


}
