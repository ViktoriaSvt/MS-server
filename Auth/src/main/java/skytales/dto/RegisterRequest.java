package AuthRoot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;

public record RegisterRequest(
        @Email(message = "Email must be valid!") String email,
        @Min(value = 8, message = "Password must be at least 8 characters long.") String password,
        String rePass
) {}
