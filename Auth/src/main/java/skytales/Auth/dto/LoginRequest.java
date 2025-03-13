package skytales.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Email(message = "Email must be valid!")
        @NotBlank(message = "Email cannot be empty!")
        String email,

        @NotBlank(message = "Password cannot be empty!")
        String password
) {
}
