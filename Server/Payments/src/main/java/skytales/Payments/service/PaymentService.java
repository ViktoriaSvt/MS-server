package skytales.Payments.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import skytales.Payments.model.Stock;
import skytales.Payments.repository.StockRepository;
import skytales.Payments.util.exception.PaymentFailedException;
import skytales.Payments.util.state_engine.UpdateProducer;
import skytales.Payments.util.state_engine.dto.BookMessage;
import skytales.Payments.web.dto.BookItem;
import skytales.Payments.model.Payment;
import skytales.Payments.model.PaymentStatus;
import skytales.Payments.repository.PaymentRepository;
import skytales.Payments.web.dto.PaymentRequest;


import java.util.*;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StockRepository stockRepository;
    private final StripeService stripeService;
    private final UpdateProducer updateProducer;

    public PaymentService(PaymentRepository paymentRepository, StockRepository stockRepository,
                          StripeService stripeService, UpdateProducer updateProducer) {
        this.paymentRepository = paymentRepository;
        this.stockRepository = stockRepository;
        this.stripeService = stripeService;
        this.updateProducer = updateProducer;
    }

    @Transactional
    public PaymentIntent processPayment(UUID userId, PaymentRequest paymentRequest) throws PaymentFailedException, StripeException {

        checkStock(paymentRequest.books());

        PaymentIntent paymentIntent = stripeService.createPaymentIntent(paymentRequest);

        handlePaymentRecord(userId, paymentRequest, paymentIntent);

        return paymentIntent;
    }

    private void checkStock(List<BookItem> books) {
        for (BookItem book : books) {
            Stock stock = stockRepository.findByBookId(UUID.fromString(book.id()));
            if (stock == null || stock.getQuantity() < 1) {
                throw new RuntimeException("Insufficient quantity for book: " + book.title());
            }
            stock.setQuantity(stock.getQuantity() - 1);
            stockRepository.save(stock);
        }
    }

    private void handlePaymentRecord(UUID userId, PaymentRequest paymentRequest, PaymentIntent paymentIntent) {
        PaymentStatus status;

        switch (paymentIntent.getStatus()) {
            case "succeeded":
                status = PaymentStatus.SUCCEEDED;
                updateProducer.clearCartForUser(userId.toString());
                break;
            case "requires_action":
            case "requires_source_action":
                status = PaymentStatus.PENDING;
                break;
            case "requires_payment_method":
            default:
                status = PaymentStatus.DENIED;
                break;
        }

        createPaymentRecord(userId, paymentRequest.amount(), paymentIntent.getId(), status, paymentRequest.books());
    }

    @Transactional
    public void createPaymentRecord(UUID userId, Long amount, String id, PaymentStatus status, List<BookItem> books) {
        Payment payment = Payment.builder()
                .createdAt(new Date())
                .paymentStatus(status)
                .amount(Double.valueOf(amount))
                .paymentIntentId(id)
                .user(userId)
                .bookTitles(new ArrayList<>())
                .build();

        if (status == PaymentStatus.SUCCEEDED) {
            books.forEach(book -> payment.getBookTitles().add(book.title()));
        }

        paymentRepository.save(payment);
    }

    public List<Payment> getAllByOwner(UUID userId) {
        return paymentRepository.findTop4ByUserOrderByCreatedAtDesc(userId);
    }

    public void addBookToState(BookMessage data) {
        Stock stock = Stock.builder().bookId(data.id()).quantity(data.quantity()).build();
        stockRepository.save(stock);
    }
}

