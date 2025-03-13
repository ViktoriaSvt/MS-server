package skytales.payment.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
@Component
public class BookState {

    private Map<UUID, BookDetails> bookStateMap = new HashMap<>();

    @Getter
    @Setter
    public static class BookDetails {
        private UUID id;
        private Integer quantity;

        public BookDetails(UUID id, Integer quantity) {
            this.id = id;
            this.quantity = quantity;
        }
    }

    public BookDetails getById(UUID bookId) {
        return bookStateMap.get(bookId);
    }

    public void setBook(UUID bookId, Integer quantity) {
        BookDetails bookDetails = bookStateMap.get(bookId);
        if (bookDetails != null) {
            bookDetails.setQuantity(quantity);
        } else {
            bookStateMap.put(bookId, new BookDetails(bookId, quantity));
        }
    }

    public void addBook(UUID bookId, Integer quantity) {
        bookStateMap.put(bookId, new BookDetails(bookId, quantity));
        System.out.println("Added book - " + bookStateMap.get(bookId));
    }
}
