package skytales.Payments.web;


import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import skytales.Payments.web.dto.PaymentRequest;
import skytales.Payments.util.exception.PaymentFailedException;
import skytales.Payments.model.Payment;
import skytales.Payments.model.PaymentStatus;
import skytales.Payments.service.PaymentService;
import skytales.Payments.service.StripeService;
import skytales.Payments.util.state_engine.UpdateProducer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {

        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest paymentRequest, HttpServletRequest request) throws StripeException {
        UUID userId = UUID.fromString(request.getAttribute("userId").toString());

        PaymentIntent paymentIntent = paymentService.processPayment(userId, paymentRequest);

        if ("requires_action".equals(paymentIntent.getStatus()) || "requires_source_action".equals(paymentIntent.getStatus())) {
            return ResponseEntity.ok(Map.of(
                    "requiresAction", true,
                    "paymentIntentClientSecret", paymentIntent.getClientSecret()
            ));
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/{userId}/history")
    public ResponseEntity<?> history(@PathVariable("userId") String userId) {

        UUID owner = UUID.fromString(userId);
        List<Payment> payments = paymentService.getAllByOwner(owner);

        return ResponseEntity.status(HttpStatus.OK).body(payments);
    }
}
