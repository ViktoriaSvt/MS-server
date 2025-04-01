package skytales.Payments.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import skytales.Payments.model.Payment;
import skytales.Payments.model.PaymentStatus;
import skytales.Payments.repository.PaymentRepository;
import skytales.Payments.web.dto.BookItem;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceUTest {



    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private UUID userId;
    private String paymentIntentId;
    private List<BookItem> books;

    @BeforeEach
    public void setUp() {
        userId = UUID.fromString("73fded46-c09b-49cf-b581-8ed145a887fe");
        paymentIntentId = "pi_1GqIC8HYgolSBA35x8q2x8bV";
        books = List.of(new BookItem("BookId", "Title1", BigDecimal.valueOf(30)));

    }

    @Test
    void retrievesAllPaymentsByOwner() {

        Payment payment = new Payment();
        payment.setUser(userId);

        List<Payment> mockPayments = List.of(payment);

        Mockito.when(paymentRepository.findByUser(userId)).thenReturn(mockPayments);

        List<Payment> result = paymentRepository.findByUser(userId);

        Assertions.assertEquals(mockPayments, result);
        Assertions.assertEquals(mockPayments.size(), result.size());
        Assertions.assertEquals(mockPayments.getFirst().getUser(), result.getFirst().getUser());
        Assertions.assertEquals(mockPayments.getLast().getUser(), result.getLast().getUser());

        verify(paymentRepository, times(1)).findByUser(userId);
    }

    @Test
    void createPaymentRecord_Success() {
        long amount = 1000L;

        PaymentStatus status = PaymentStatus.SUCCEEDED;
        paymentService.createPaymentRecord(userId, amount, paymentIntentId, status, books);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(1)).save(paymentCaptor.capture());

        Payment capturedPayment = paymentCaptor.getValue();
        Assertions.assertNotNull(capturedPayment);
        Assertions.assertEquals(userId, capturedPayment.getUser());
        Assertions.assertEquals(Double.valueOf(amount), capturedPayment.getAmount());
        Assertions.assertEquals(paymentIntentId, capturedPayment.getPaymentIntentId());
        Assertions.assertEquals(status, capturedPayment.getPaymentStatus());
        Assertions.assertEquals(books.size(), capturedPayment.getBookTitles().size());
        Assertions.assertEquals(books.getFirst().title(), capturedPayment.getBookTitles().getFirst());
    }

    @Test
    void createPaymentRecord_Failed() {
        long amount = 1000L;

        PaymentStatus status = PaymentStatus.DENIED;
        paymentService.createPaymentRecord(userId, amount, paymentIntentId, status, books);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(1)).save(paymentCaptor.capture());

        Payment capturedPayment = paymentCaptor.getValue();
        Assertions.assertNotNull(capturedPayment);
        Assertions.assertEquals(userId, capturedPayment.getUser());
        Assertions.assertEquals(Double.valueOf(amount), capturedPayment.getAmount());
        Assertions.assertEquals(paymentIntentId, capturedPayment.getPaymentIntentId());
        Assertions.assertEquals(status, capturedPayment.getPaymentStatus());
        Assertions.assertTrue(capturedPayment.getBookTitles().isEmpty());
    }



}
