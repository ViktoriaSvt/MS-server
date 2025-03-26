package skytales.Carts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import skytales.Carts.model.BookItemReference;
import skytales.Carts.repository.BookItemReferenceRepository;
import skytales.Carts.util.state_engine.dto.BookMessage;

import java.math.BigDecimal;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ReferenceServiceUTest {

    @Mock
    private BookItemReferenceRepository bookItemReferenceRepository;

    @InjectMocks
    private BookReferenceService bookReferenceService;

    private BookMessage bookRequest;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        bookRequest = new BookMessage(bookId, "Title", "Fantasy", "Author", "http://example.com/cover.jpg", 2000, BigDecimal.valueOf(1), 30);
    }

    @Test
    void addBookToState() {
        bookReferenceService.addBookToState(bookRequest);

        verify(bookItemReferenceRepository, times(1)).save(any(BookItemReference.class));
    }

    @Test
    void removeBookFromState() {
        BookItemReference bookItemReference = new BookItemReference();
        bookItemReference.setId(bookId);

        when(bookItemReferenceRepository.findById(bookId)).thenReturn(Optional.of(bookItemReference));

        bookReferenceService.removeBookFromState(bookRequest);

        verify(bookItemReferenceRepository, times(1)).delete(bookItemReference);
    }

    @Test
    void removeBookFromState_BookNotFound() {

        when(bookItemReferenceRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            bookReferenceService.removeBookFromState(bookRequest);
        });

        verify(bookItemReferenceRepository, never()).delete(any(BookItemReference.class));
    }


}
