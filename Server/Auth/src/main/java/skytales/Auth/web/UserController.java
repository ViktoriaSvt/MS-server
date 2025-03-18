package skytales.Auth.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import skytales.Auth.web.dto.UserListItem;
import skytales.Auth.model.User;
import skytales.Auth.service.AuthService;
import skytales.Auth.service.UserService;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/users")
public class UserController {


    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable("id") String id) {

        UUID userId = UUID.fromString(id);
        User user = userService.getById(userId);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        List<UserListItem> users = userService.listUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/promote")
    public ResponseEntity<?> promote(@RequestParam String id) {

        boolean success = userService.giveAdminRole(id);

        if (success) {
            return ResponseEntity.ok("User promoted successfully");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

    }

    @PutMapping("/demote")
    public ResponseEntity<?> demote(@RequestParam String id) {

        boolean success = userService.giveUserRole(id);

        if (success) {
            return ResponseEntity.ok("User demoted successfully");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

    }
}
