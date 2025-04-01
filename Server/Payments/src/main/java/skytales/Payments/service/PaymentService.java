package skytales.Payments.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import skytales.Payments.model.Stock;
import skytales.Payments.repository.StockRepository;
import skytales.Payments.util.state_engine.dto.BookMessage;
import skytales.Payments.web.dto.BookItem;
import skytales.Payments.model.Payment;
import skytales.Payments.model.PaymentStatus;
import skytales.Payments.repository.PaymentRepository;


import java.util.*;

@Service
public class PaymentService {


    private final PaymentRepository paymentRepository;
    private final StockRepository stockRepository;

    public PaymentService(PaymentRepository paymentRepository, StockRepository stockRepository) {

        this.paymentRepository = paymentRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public void sufficientQuantity(List<BookItem> books) {

        books.forEach((book) -> {

            Stock stock = stockRepository.findByBookId(UUID.fromString(book.id()));
            int quantity = stock.getQuantity();

            if ( quantity < 1) {
                throw new RuntimeException("Insufficient quantity");
            }

            stock.setQuantity(quantity - 1);

        });


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

    public void getBookState() {
        stockRepository.findAll();
    }

    public void addBookToState(BookMessage data) {

        Stock stock = Stock.builder()
                .bookId(data.id())
                .quantity(data.quantity())
                .build();

        stockRepository.save(stock);
    }
}
