package skytales.Payments.service;

import com.stripe.model.PaymentIntent;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import skytales.Payments.util.exception.PaymentFailedException;
import skytales.Payments.web.dto.BookItem;
import skytales.Payments.web.dto.PaymentRequest;


import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "stripe.secret.key=sk_test_51QcrYKJdrx2Bl88huhlvnfqPxrqBmfo9BM6wxg0mlYJugCMEpw9CHlspF8I9tTEzL0gq9NeWcFTNCEoLgDjMTbfu00idvkIYJK")
public class StripeServiceITest {

    private StripeService stripeService;

    @Value("${stripe.secret.key}")
    private String testSecretKey;

    @BeforeEach
    void setUp() {
        Stripe.apiKey = testSecretKey;
        stripeService = new StripeService();
    }

    @Test
    void createPaymentIntent_ShouldReturnValidIntent_WhenPaymentIsSuccessful() throws StripeException {

        List<BookItem> books = Collections.emptyList();
        PaymentRequest request = new PaymentRequest("pm_card_visa", 1000L, books);

        PaymentIntent paymentIntent = stripeService.createPaymentIntent(request);

        assertNotNull(paymentIntent);
        assertEquals("succeeded", paymentIntent.getStatus());
        assertEquals(100000, paymentIntent.getAmount());
    }

    @Test
    void createPaymentIntent_ShouldThrowPaymentFailedException_WhenCardIsDeclined() {


        List<BookItem> books = Collections.emptyList();
        PaymentRequest request = new PaymentRequest("pm_card_visa_chargeDeclined", 1000L, books);

        PaymentFailedException exception = assertThrows(PaymentFailedException.class, () -> {
            stripeService.createPaymentIntent(request);
        });

        assertEquals("Payment failed due to insufficient funds.", exception.getMessage());
    }

}
