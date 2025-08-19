package skytales.Payments.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import skytales.Payments.config.TestConfig;
import skytales.Payments.model.Payment;
import skytales.Payments.model.PaymentStatus;
import skytales.Payments.service.PaymentService;
import skytales.Payments.service.StripeService;
import skytales.Payments.util.config.security.SecurityConfig;
import skytales.Payments.util.exception.PaymentFailedException;
import skytales.Payments.util.state_engine.UpdateProducer;
import skytales.Payments.web.PaymentController;
import skytales.Payments.web.dto.BookItem;
import skytales.Payments.web.dto.PaymentRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "testuser", roles = {"USER"})
@Import({SecurityConfig.class, TestConfig.class})
@ExtendWith(MockitoExtension.class)
@WebMvcTest(PaymentController.class)
public class PaymentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private StripeService stripeService;

    @MockitoBean
    private UpdateProducer updateProducer;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("73fded46-c09b-49cf-b581-8ed145a887fe");

        paymentRequest = new PaymentRequest(
                "pm_card_visa",
                1000L,
                List.of(new BookItem("BookId", "Title1", BigDecimal.valueOf(30)))
        );

        Mockito.doNothing().when(updateProducer).clearCartForUser(Mockito.anyString());
    }

    @Test
    void testProcessPayment_Success() throws Exception {
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setId("pi_1GqIC8HYgolSBA35x8q2x8bV");
        paymentIntent.setStatus("succeeded");

        Mockito.when(paymentService.processPayment(Mockito.any(UUID.class), Mockito.any(PaymentRequest.class)))
                .thenReturn(paymentIntent);

        Mockito.when(stripeService.createPaymentIntent(Mockito.any(PaymentRequest.class))).thenReturn(paymentIntent);

        String paymentRequestJson = objectMapper.writeValueAsString(paymentRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequestJson)
                        .requestAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true}"));
    }

    @Test
    void testProcessPayment_Failed() throws Exception {
        Mockito.when(paymentService.processPayment(Mockito.any(UUID.class), Mockito.any(PaymentRequest.class)))
                .thenThrow(new PaymentFailedException("Payment failed due to insufficient funds."));

        String paymentRequestJson = objectMapper.writeValueAsString(paymentRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequestJson)
                        .requestAttr("userId", userId))
                .andExpect(status().isPaymentRequired())
                .andExpect(content().string("Payment failed due to insufficient funds."));
    }

    @Test
    void testHistory() throws Exception {
        List<Payment> payments = List.of(new Payment());

        Mockito.when(paymentService.getAllByOwner(Mockito.any(UUID.class))).thenReturn(payments);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/payments/{userId}/history", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(payments)));
    }
}
