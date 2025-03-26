package skytales.Auth.util.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("yJhY2zA6WxVr8PqWNxQtbk5U4v3iSz1A7ghz6j9kPZJXy9U2w")
    private String secretKey;


    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {


        String token = extractJwtFromRequest(request);

        if (token != null && isTokenValid(token)) {
            String userId = extractUserId(token);
            String username = extractUsername(token);
            String role = extractRole(token);
            String email = extractEmail(token);
            String cartId = extractCartId(token);

            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.setAttribute("role", role);
            request.setAttribute("cartId", cartId);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, null);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    private String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    private String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    private String extractCartId(String token) {
        return extractClaim(token, claims -> claims.get("cartId", String.class));
    }

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

}
