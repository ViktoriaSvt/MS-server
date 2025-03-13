package skytales.auth.dto;

public record RegisterResponse(String email, String userId, String role, String jwtToken) {}

