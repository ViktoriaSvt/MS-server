package skytales.Auth.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email(message = "Email must be valid!") String email,
        @Size(min = 8, message = "Password must be at least 8 characters long.") String password,
        String rePass
) {}
