package skytales.Library.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import skytales.Library.model.Book;
import skytales.Library.repository.BookRepository;
import skytales.Library.util.elasticsearch.service.ElasticSearchService;
import skytales.Library.util.exceptions.BookNotFoundException;
import skytales.Library.util.state_engine.UpdateProducer;
import skytales.Library.util.state_engine.model.UpdateType;
import skytales.Library.web.dto.BookData;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceUTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ElasticSearchService elasticSearchService;

    @Mock
    private BookService bookServiceTest;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private UpdateProducer updateProducer;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        book = Book.builder()
                .id(bookId)
                .title("Test Book")
                .author("Test Author")
                .coverImageUrl("cover.jpg")
                .bannerImageUrl("banner.jpg")
                .price(new BigDecimal("19.99"))
                .description("A test book")
                .year(2024)
                .genre("Fiction")
                .quantity(10)
                .build();
    }

    @Test
    void getAllBooks_ShouldReturnBookList() {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> books = bookService.getAllBooks();

        assertNotNull(books);
        assertEquals(1, books.size());
        assertEquals("Test Book", books.getFirst().getTitle());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void getBookById_ShouldReturnBook_WhenBookExists() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        Book foundBook = bookService.getBookById(bookId);

        assertNotNull(foundBook);
        assertEquals(bookId, foundBook.getId());
        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    void getBookById_ShouldThrowException_WhenBookDoesNotExist() {

        UUID bookId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        BookNotFoundException exception = assertThrows(
                BookNotFoundException.class,
                () -> bookService.getBookById(bookId)
        );

        assertEquals("No book found with id " + bookId, exception.getMessage());
        verify(bookRepository, times(1)).findById(bookId);
    }


    @Test
    void createBook_ShouldSaveBookAndSendUpdates() throws IOException {
        BookData bookData = new BookData("Test Book", "Test Author", "Some kind of description is here", "2007", "19", "10", "Some Description");
        when(cloudinary.uploader()).thenReturn(uploader);

        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenAnswer(invocation -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("secure_url", "http://example.com/cover.jpg");
                    return result;
                });

        Book book = Book.builder()
                .title("Test Book")
                .author("Test Author")
                .coverImageUrl("http://example.com/cover.jpg")
                .bannerImageUrl("http://example.com/banner.jpg")
                .price(new BigDecimal("19.99"))
                .description("Some kind of description is here")
                .year(2007)
                .genre("Fiction")
                .quantity(10)
                .build();

        when(bookRepository.save(any(Book.class))).thenReturn(book);
        doNothing().when(updateProducer).sendBookUpdate(eq(UpdateType.NEW_BOOK), any(Book.class));

        MultipartFile bannerImage = mock(MultipartFile.class);
        MultipartFile coverImage = mock(MultipartFile.class);

        when(coverImage.getOriginalFilename()).thenReturn("cover.jpg");
        when(bannerImage.getOriginalFilename()).thenReturn("banner.jpg");
        when(coverImage.getBytes()).thenReturn("coverImageContent".getBytes());
        when(bannerImage.getBytes()).thenReturn("bannerImageContent".getBytes());

        Book createdBook = bookService.createBook(bookData, bannerImage, coverImage);

        assertNotNull(createdBook);
        assertEquals("Test Book", createdBook.getTitle());

        verify(bookRepository, times(1)).save(any(Book.class));
        verify(updateProducer, times(1)).sendBookUpdate(eq(UpdateType.NEW_BOOK), any(Book.class));
        verify(uploader, times(2)).upload(any(byte[].class), any(Map.class));
    }

}
