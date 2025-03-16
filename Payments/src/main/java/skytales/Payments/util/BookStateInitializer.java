package skytales.Payments.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import skytales.Payments.model.BookState;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
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
        String url = "http://localhost:8085/books";
        try {
            ResponseEntity<BookDetailsDto[]> response = restTemplate.getForEntity(url, BookDetailsDto[].class);
            List<BookDetailsDto> books = Arrays.asList(response.getBody());
            books.forEach(book -> bookState.setBook(book.id, book.quantity));

            log.info("Books fetched successfully");
        } catch (Exception e) {
            log.error(e.getMessage());
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
