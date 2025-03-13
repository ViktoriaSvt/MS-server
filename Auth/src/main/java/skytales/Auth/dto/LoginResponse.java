package skytales.auth.dto;
public record LoginResponse(String userId, String username, String role, String jwtToken) {}
