package skytales.Payments.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import skytales.Payments.web.dto.PaymentRequest;
import skytales.Payments.util.exception.PaymentFailedException;


@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String secretKey;

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
                            .setReturnUrl("${stripe.returnUrl}")
                            .build()
            );

        } catch (StripeException e) {
            throw new PaymentFailedException(e.getCode().equals("card_declined")
                    ? "Payment failed due to insufficient funds"
                    : e.getMessage());
        }
    }



}
