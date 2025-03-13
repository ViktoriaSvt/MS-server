package skytales.common.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;
import skytales.auth.dto.SessionResponse;

@Service
public class SessionService {
    public SessionResponse getSessionData(HttpServletRequest request) {

         String userId = (String) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        String email = (String) request.getAttribute("email");
        String role = (String) request.getAttribute("role");
        String cartId = (String) request.getAttribute("cartId");

        if (userId == null || username == null || email == null || role == null) {
            throw new SessionAuthenticationException("Session data is incomplete.");
        }

        return new SessionResponse(email, username, userId, role, cartId);
    }
}
