package skytales.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skytales.payment.dto.BookItem;
import skytales.payment.model.BookState;
import skytales.payment.model.Payment;
import skytales.payment.model.PaymentStatus;
import skytales.payment.repository.PaymentRepository;
import java.util.*;

@Service
public class PaymentService {

    private final BookState bookState;
    private final PaymentRepository paymentRepository;

    public PaymentService(BookState bookState, PaymentRepository paymentRepository) {
        this.bookState = bookState;
        this.paymentRepository = paymentRepository;
    }

    public void addBookToState(UUID id, int quantity) {
        bookState.addBook(id, quantity);
    }

    @Transactional
    public void sufficientQuantity(List<BookItem> books) {

        if(books.isEmpty()) {
            throw new RuntimeException("Nothing to purchase!");
        }

        for (BookItem book : books) {
            UUID bookId = UUID.fromString(book.bookId());
            BookState.BookDetails bookDetails = bookState.getById(bookId);

            if (bookDetails == null || bookDetails.getQuantity() <= 0) {
                throw new RuntimeException("Insufficient stock for book: " + book.title());
            }

            bookState.setBook(bookId, bookDetails.getQuantity() - 1);
        }

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

        if(status == PaymentStatus.SUCCEEDED) {
           books.forEach(book ->  payment.getBookTitles().add(book.title()));
        }

        paymentRepository.save(payment);
    }

    public List<Payment> getAllByOwner(UUID userId) {
      return  paymentRepository.findByUser(userId);
    }
}
