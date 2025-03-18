package skytales.Auth.web.dto;

public record SessionResponse(String email,String username, String id, String role, String cartId) { }

