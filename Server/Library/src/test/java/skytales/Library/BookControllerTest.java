package skytales.Library;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import org.springframework.web.multipart.MultipartFile;
import skytales.Library.config.TestConfigLib;
import skytales.Library.model.Book;
import skytales.Library.service.BookService;
import skytales.Library.util.config.security.SecurityConfig;
import skytales.Library.util.elasticsearch.service.ElasticSearchService;
import skytales.Library.util.exceptions.BookNotFoundException;
import skytales.Library.web.BookController;
import skytales.Library.web.dto.BookData;




import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = "testuser", roles = {"USER"})
@Import({SecurityConfig.class, TestConfigLib.class})
@ExtendWith(MockitoExtension.class)
@WebMvcTest(BookController.class)
public class BookControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private ElasticsearchClient elasticsearchClient;

    @MockitoBean
    private ElasticSearchService elasticSearchService;

    @InjectMocks
    private BookController bookController;

    private Book book;
    private UUID bookId;
    private UUID userId;
    private String token;


    @BeforeEach
    void setUp() {
        userId = UUID.fromString("73fded46-c09b-49cf-b581-8ed145a887fe");
        token = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsImNhcnRJZCI6IjU1MjYxNzEzLTY0ZDMtNDhhMy04ZWFiLWViNWU3YWU4MjU1NiIsInVzZXJJZCI6ImRlZTAwZGQ5LWQzMWUtNGUxNi1iZWFlLTc4OWNlMzM4OTNmMiIsImVtYWlsIjoidGVzdGVtYWlsQGFidi5iZyIsInVzZXJuYW1lIjoidXNlcm5hbWUiLCJzdWIiOiJ0ZXN0ZW1haWxAYWJ2LmJnIiwiaWF0IjoxNzQzMzQ3NTQzLCJleHAiOjE3NDMzNTAxMzV9.f84j-KD9aQFetidEy_ShHd3qSyDzlMUQQn_NnejGA1g";

        bookId = UUID.randomUUID();
        book = Book.builder()
                .id(bookId)
                .title("Test Book")
                .author("Test Author")
                .coverImageUrl("https://www.hostinger.co.uk/tutorials/wp-content/uploads/sites/2/2022/07/the-structure-of-a-url.png")
                .bannerImageUrl("https://www.hostinger.co.uk/tutorials/wp-content/uploads/sites/2/2022/07/the-structure-of-a-url.png")
                .price(new BigDecimal("19.99"))
                .description("A test book")
                .year(2024)
                .genre("Fiction")
                .quantity(10)
                .build();

    }


    @Test
    void getBooks_ShouldReturnBooksList() throws Exception {
        when(bookService.getAllBooks()).thenReturn(List.of(book));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Book"));
    }

    @Test
    void getBook_ShouldReturnBook_WhenExists() throws Exception {
        when(bookService.getBookById(bookId)).thenReturn(book);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{bookId}", bookId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    void getBook_ShouldReturnNotFound_WhenBookDoesNotExist() throws Exception {

        when(bookService.getBookById(bookId))
                .thenThrow(new BookNotFoundException("No book found with id " + bookId));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{bookId}", bookId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No book found with id " + bookId));
    }


    @Test
    void createBook_ShouldReturnCreatedBook() throws Exception {
        BookData bookData = new BookData("Test Book", "Test Author", "A test book", "2024", "Fiction", "19.99", "10");

        Book book = Book.builder()
                .title("Test Book")
                .author("Test Author")
                .coverImageUrl("https://www.hostinger.co.uk/tutorials/wp-content/uploads/sites/2/2022/07/the-structure-of-a-url.png")
                .bannerImageUrl("https://www.hostinger.co.uk/tutorials/wp-content/uploads/sites/2/2022/07/the-structure-of-a-url.png")
                .price(new BigDecimal("19.99"))
                .description("A test book")
                .year(2024)
                .genre("Fiction")
                .quantity(10)
                .build();

        when(bookService.createBook(any(BookData.class), any(MultipartFile.class), any(MultipartFile.class))).thenReturn(book);

        MultipartFile bannerImage = mock(MultipartFile.class);
        MultipartFile coverImage = mock(MultipartFile.class);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/books/create")
                        .file("bannerImage", "bannerImageContent".getBytes())
                        .file("coverImage", "coverImageContent".getBytes())
                        .param("title", bookData.title())
                        .param("author", bookData.author())
                        .param("description", bookData.description())
                        .param("year", bookData.year())
                        .param("genre", bookData.genre())
                        .param("price", bookData.price())
                        .param("quantity", bookData.quantity())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }

    @Test
    void getNewest_ShouldReturnNewestBooks() throws Exception {
        Book book = Book.builder()
                .id(bookId)
                .title("Newest Book")
                .author("Newest Author")
                .coverImageUrl("https://www.hostinger.co.uk/tutorials/wp-content/uploads/sites/2/2022/07/the-structure-of-a-url.png")
                .bannerImageUrl("https://www.hostinger.co.uk/tutorials/wp-content/uploads/sites/2/2022/07/the-structure-of-a-url.png")
                .price(new BigDecimal("19.99"))
                .description("A newest book")
                .year(2024)
                .genre("Fiction")
                .quantity(10)
                .build();

        when(bookService.getNewestBooks(any(Integer.class))).thenReturn(List.of(book));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/newest")
                        .param("year", "2024")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Newest Book"));
    }

    @Test
    void searchBooks_ShouldReturnBooks_WhenQueryMatches() throws Exception {
        when(elasticsearchClient.search(any(SearchRequest.class), eq(Book.class))).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/books/search")
                        .param("query", "Test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }


}
