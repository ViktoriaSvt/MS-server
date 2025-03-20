package skytales.Auth.web.dto;
public record LoginResponse(String userId, String email, String role, String jwtToken) {}
