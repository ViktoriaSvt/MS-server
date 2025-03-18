package skytales.Payments.service;

import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import skytales.Payments.util.exception.PaymentFailedException;
import skytales.Payments.web.dto.BookItem;
import skytales.Payments.web.dto.PaymentRequest;


import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StripeServiceUTest {

    private static final String TEST_STRIPE_KEY = "sk_test_51QcrYKJdrx2Bl88huhlvnfqPxrqBmfo9BM6wxg0mlYJugCMEpw9CHlspF8I9tTEzL0gq9NeWcFTNCEoLgDjMTbfu00idvkIYJK";

    @Mock
    private StripeException stripeException;

    @InjectMocks
    private StripeService stripeService;

    private PaymentRequest paymentRequest;

    @BeforeEach
    public void setUp() throws Exception {
        setStripeApiKey(TEST_STRIPE_KEY);
        stripeService.init();
        paymentRequest = new PaymentRequest("pm_card_visa", 1000L, List.of(new BookItem("BookId", "Title1", BigDecimal.valueOf(30))));
    }

    private void setStripeApiKey(String apiKey) throws Exception {
        Field secretKeyField = StripeService.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(stripeService, apiKey);
    }

    @Test
    public void testCreatePaymentIntent_Success() {
        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);

        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(mockPaymentIntent);

            PaymentIntent result = stripeService.createPaymentIntent(paymentRequest);

            assertNotNull(result);
            mockedStatic.verify(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)), times(1));
        }
    }

    @Test
    public void testCreatePaymentIntent_CardDeclined() throws StripeException {

        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenAnswer(invocation -> {
                        throw new CardException(
                                "Your card was declined.",
                                "request-id-123",
                                "card_error",
                                null,
                                "card_declined",
                                null,
                                402,
                                null
                        );
                    });

            PaymentFailedException exception = assertThrows(PaymentFailedException.class, () -> stripeService.createPaymentIntent(paymentRequest));
            assertEquals("Your card was declined.; code: card_error; request-id: request-id-123", exception.getMessage());

            mockedStatic.verify(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)), times(1));
        }
    }

}
