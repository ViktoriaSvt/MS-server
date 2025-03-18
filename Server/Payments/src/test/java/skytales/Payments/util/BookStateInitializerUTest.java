package skytales.Payments.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import skytales.Payments.model.BookState;


import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookStateInitializerUTest {

    @Mock
    private BookState bookState;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BookStateInitializer bookStateInitializer;

    private BookStateInitializer.BookDetailsDto[] bookDetailsDtos;

    @BeforeEach
    void setUp() {
        bookDetailsDtos = new BookStateInitializer.BookDetailsDto[]{
                new BookStateInitializer.BookDetailsDto(UUID.randomUUID(), 10),
                new BookStateInitializer.BookDetailsDto(UUID.randomUUID(), 5)
        };
    }

    @Test
    void onApplicationEvent() {
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
        bookStateInitializer.onApplicationEvent(event);

        verify(restTemplate, times(1)).getForEntity(anyString(), eq(BookStateInitializer.BookDetailsDto[].class));
    }

    @Test
    void fetchBooksOnStartup_Success() {
        ResponseEntity<BookStateInitializer.BookDetailsDto[]> responseEntity = new ResponseEntity<>(bookDetailsDtos, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(BookStateInitializer.BookDetailsDto[].class))).thenReturn(responseEntity);

        bookStateInitializer.fetchBooksOnStartup();

        for (BookStateInitializer.BookDetailsDto book : bookDetailsDtos) {
            verify(bookState, times(1)).setBook(book.id(), book.quantity());
        }
    }

    @Test
    void fetchBooksOnStartup_Failure() {
        when(restTemplate.getForEntity(anyString(), eq(BookStateInitializer.BookDetailsDto[].class))).thenThrow(new RuntimeException("Failed to fetch books"));

        bookStateInitializer.fetchBooksOnStartup();

        verify(bookState, never()).setBook(any(UUID.class), anyInt());
    }
}
