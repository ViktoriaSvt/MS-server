package skytales.payment;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import skytales.auth.dto.SessionResponse;
import skytales.common.kafka.state_engine.UpdateProducer;
import skytales.common.security.SessionService;
import skytales.payment.dto.PaymentRequest;
import skytales.payment.exception.PaymentFailedException;
import skytales.payment.model.Payment;
import skytales.payment.model.PaymentStatus;
import skytales.payment.service.PaymentService;
import skytales.payment.service.StripeService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class Payments {


    private final SessionService sessionService;
    private final PaymentService paymentService;
    private final StripeService stripeService;
    private final UpdateProducer updateProducer;

    public Payments(SessionService sessionService, PaymentService paymentService, StripeService stripeService, UpdateProducer updateProducer) {
        this.sessionService = sessionService;
        this.paymentService = paymentService;
        this.stripeService = stripeService;
        this.updateProducer = updateProducer;
    }


    @PostMapping()
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest paymentRequest, HttpServletRequest request) throws StripeException {
        SessionResponse sessionResponse = sessionService.getSessionData(request);
        UUID userId = UUID.fromString(sessionResponse.id());
        PaymentIntent paymentIntent;

        paymentService.sufficientQuantity(paymentRequest.books());
        try {
            paymentIntent = stripeService.createPaymentIntent(paymentRequest);
        } catch (PaymentFailedException e) {
            paymentService.createPaymentRecord(userId, paymentRequest.amount(), null, PaymentStatus.DENIED, paymentRequest.books());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(e.getMessage());
        }

        if ("requires_action".equals(paymentIntent.getStatus()) || "requires_source_action".equals(paymentIntent.getStatus())) {
            paymentService.createPaymentRecord(userId, paymentRequest.amount(), paymentIntent.getId(), PaymentStatus.PENDING, paymentRequest.books());
            return ResponseEntity.ok().body(Map.of(
                    "error", "payment failed",
                    "requiresAction", true,
                    "paymentIntentClientSecret", paymentIntent.getClientSecret()
            ));
        }

        if ("requires_payment_method".equals(paymentIntent.getStatus())) {
            paymentService.createPaymentRecord(userId, paymentRequest.amount(), paymentIntent.getId(), PaymentStatus.DENIED, paymentRequest.books());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(Map.of("error", "Payment failed. Authentication or payment method issue."));
        }

        updateProducer.clearCartForUser(String.valueOf(userId));
        paymentService.createPaymentRecord(userId, paymentRequest.amount(), paymentIntent.getId(), PaymentStatus.SUCCEEDED, paymentRequest.books());
        return ResponseEntity.ok().body(Map.of("success", true));
    }


    @GetMapping("/{userId}/history")
    public ResponseEntity<?> history(@PathVariable String userId){

        UUID owner = UUID.fromString(userId);
        List<Payment> payments = paymentService.getAllByOwner(owner);

        return ResponseEntity.status(HttpStatus.OK).body(payments);
    }
}
