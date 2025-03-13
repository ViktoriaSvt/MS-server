package skytales.payment.util;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import skytales.payment.model.BookState;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class BookStateInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final BookState bookState;
    private final RestTemplate restTemplate;

    public BookStateInitializer(BookState bookState, RestTemplate restTemplate) {
        this.bookState = bookState;
        this.restTemplate = restTemplate;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        fetchBooksOnStartup();
    }

    public void fetchBooksOnStartup() {
        String url = "http://localhost:8080/books";
        try {
            ResponseEntity<BookDetailsDto[]> response = restTemplate.getForEntity(url, BookDetailsDto[].class);
            List<BookDetailsDto> books = Arrays.asList(response.getBody());
            books.forEach(book -> bookState.setBook(book.id, book.quantity));

            System.out.println("Loaded books into BookState.");
        } catch (Exception e) {
            System.out.println("Failed to fetch books: " + e.getMessage());
        }
    }

    public record BookDetailsDto(UUID id, Integer quantity) {
        @Override
        public String toString() {
            return "BookDetailsDto{" +
                    "id=" + id +
                    ", quantity=" + quantity +
                    '}';
        }
    }
}
