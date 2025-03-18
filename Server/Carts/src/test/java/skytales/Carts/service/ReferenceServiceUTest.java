package skytales.Carts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import skytales.Carts.repository.BookItemReferenceRepository;
import skytales.Carts.web.dto.BookRequest;


import java.math.BigDecimal;

import java.util.UUID;



@ExtendWith(MockitoExtension.class)
class ReferenceServiceUTest {

    @Mock
    private BookItemReferenceRepository bookItemReferenceRepository;

    @InjectMocks
    private BookReferenceService bookReferenceService;

    private BookRequest bookRequest;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        bookRequest = new BookRequest(bookId, "Title", "Fantasy", "Author", "http://example.com/cover.jpg", 2000, BigDecimal.valueOf(1), 30);
    }

//    @Test
//    void addBookToState() {
//        bookReferenceService.addBookToState(bookRequest);
//
//        verify(bookItemReferenceRepository, times(1)).save(any(BookItemReference.class));
//    }

//    @Test
//    void removeBookFromState() {
//        BookItemReference bookItemReference = new BookItemReference();
//        bookItemReference.setBookId(bookId);
//
//        when(bookItemReferenceRepository.findById(bookId)).thenReturn(Optional.of(bookItemReference));
//
//        bookReferenceService.removeBookFromState(bookRequest);
//
//        verify(bookItemReferenceRepository, times(1)).delete(bookItemReference);
//    }

//    @Test
//    void removeBookFromState_BookNotFound() {
//        when(bookItemReferenceRepository.findById(bookId)).thenReturn(Optional.empty());
//
//        bookReferenceService.removeBookFromState(bookRequest);
//
//        verify(bookItemReferenceRepository, never()).delete(any(BookItemReference.class));
//    }
}
