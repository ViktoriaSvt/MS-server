package skytales.Carts.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import skytales.Carts.model.BookItemReference;
import skytales.Carts.service.CartService;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {

        this.cartService = cartService;
    }

    @PutMapping("/add/{id}")
    public ResponseEntity<?> addToCart(@PathVariable String id, HttpServletRequest request) throws JsonProcessingException {

        String attribute = request.getAttribute("cartId").toString();
        UUID cartId = UUID.fromString(attribute);
        cartService.addToCart(cartId, UUID.fromString(id));

        return ResponseEntity.ok(cartId);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<?> removeFromCart(@PathVariable String id, HttpServletRequest request) {

        UUID cartId = UUID.fromString(request.getAttribute("cartId").toString());
        cartService.deleteFromCart(cartId, UUID.fromString(id));

        return ResponseEntity.ok(cartId);
    }

    @GetMapping("/items")
    public ResponseEntity<?> getItems(HttpServletRequest request) {

        UUID cartId = UUID.fromString(request.getAttribute("cartId").toString());
        Set<BookItemReference> items = cartService.getCartItems(cartId);

        return ResponseEntity.ok(items);
    }

    @PostMapping("/createCart")
    public ResponseEntity<Map<String, String>> createCart(@RequestBody Map<String, String> requestBody) {

        String userId = requestBody.get("userId");

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "User ID is required"));
        }
        String cartId = cartService.createCartForUser(userId);

        return ResponseEntity.ok(Collections.singletonMap("cartId", cartId));
    }
}
