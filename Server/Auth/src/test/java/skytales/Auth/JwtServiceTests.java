package skytales.Auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import skytales.Auth.service.JwtService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTests {

    @InjectMocks
    private JwtService jwtService;


    private String secretKey;
    private long jwtExpiration;

    @BeforeEach
    void setUp() {
        secretKey = "yJhY2zA6WxVr8PqWNxQtbk5U4v3iSz1A7ghz6j9kPZJXy9U2w";
        jwtExpiration = 86400000;

        jwtService.setSecretKey(secretKey);
        jwtService.setJwtExpiration(jwtExpiration);
    }

    @Test
    void testGenerateToken() {
        String userId = "123";
        String role = "admin";
        String email = "test@example.com";
        String username = "testuser";
        String cartId = "abc123";

        String token = jwtService.generateToken(userId, role, email, username, cartId);

        assertNotNull(token);
        assertTrue(token.startsWith("eyJ"));
    }

    @Test
    void testExtractUsername() {
        String token = generateTestToken();

        String username = jwtService.extractUsername(token);

        assertEquals("test@example.com", username);
    }

    @Test
    void testIsTokenValid_WhenTokenIsNotExpired() {
        String token = generateTestToken();

        boolean isValid = jwtService.isTokenValid(token);

        assertTrue(isValid);
    }

    @Test
    void testIsTokenValid_WhenTokenIsExpired() {
        String expiredToken = generateExpiredTestToken();

        boolean isValid = jwtService.isTokenValid(expiredToken);

        assertFalse(isValid);
    }

    @Test
    void testIsTokenExpired_WhenTokenIsExpired() {
        String expiredToken = generateExpiredTestToken();

        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.isTokenExpired(expiredToken);
        });
    }

    @Test
    void testExtractUserId() {
        String token = generateTestToken();

        String userId = jwtService.extractUserId(token);

        assertEquals("123", userId);
    }

    @Test
    void testExtractCartId() {
        String token = generateTestToken();

        String cartId = jwtService.extractCartId(token);

        assertEquals("abc123", cartId);
    }

    @Test
    void testExtractRole() {
        String token = generateTestToken();

        String role = jwtService.extractRole(token);

        assertEquals("admin", role);
    }

    @Test
    void testExtractEmail() {
        String token = generateTestToken();

        String email = jwtService.extractEmail(token);

        assertEquals("test@example.com", email);
    }

    private String generateTestToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "123");
        claims.put("role", "admin");
        claims.put("email", "test@example.com");
        claims.put("username", "testuser");
        claims.put("cartId", "abc123");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject("test@example.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateExpiredTestToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "123");
        claims.put("role", "admin");
        claims.put("email", "test@example.com");
        claims.put("username", "testuser");
        claims.put("cartId", "abc123");

        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject("test@example.com")
                .setIssuedAt(new Date(now - 3600000))
                .setExpiration(new Date(now - 2000))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
}

