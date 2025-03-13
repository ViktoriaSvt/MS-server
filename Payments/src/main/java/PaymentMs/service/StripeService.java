package skytales.payment.service;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.exception.StripeException;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import skytales.payment.dto.PaymentRequest;
import skytales.payment.exception.PaymentFailedException;
import skytales.payment.model.BookState;

@Service
public class StripeService {

    private final BookState bookState;
    @Value("${stripe.secret.key}")
    private String secretKey;

    public StripeService(BookState bookState) {
        this.bookState = bookState;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public PaymentIntent createPaymentIntent(PaymentRequest paymentRequest) {
        try {
            long amount = paymentRequest.amount();

            return PaymentIntent.create(
                    new PaymentIntentCreateParams.Builder()
                            .setAmount(amount * 100)
                            .setCurrency("usd")
                            .setPaymentMethod(paymentRequest.paymentMethodId())
                            .setConfirm(true)
                            .setAutomaticPaymentMethods(
                                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                            )
                            .setReturnUrl("http://localhost:5173/cart")
                            .build()
            );

        } catch (StripeException e) {
            if ("card_declined".equals(e.getCode())) {
                throw new PaymentFailedException("Payment failed due to insufficient funds.");
            } else {
                throw new PaymentFailedException(e.getMessage());
            }
        }
    }



}
