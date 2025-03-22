//package skytales.Library;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch.core.SearchRequest;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//
//import skytales.Library.config.TestConfigLib;
//import skytales.Library.model.Book;
//import skytales.Library.service.BookService;
//import skytales.Library.web.BookController;
//import skytales.Library.web.dto.BookData;
//import skytales.common.configuration.SecurityConfig;
//import skytales.common.security.SessionResponse;
//
//
//
//import java.io.File;
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//
//@Import({SecurityConfig.class, TestConfigLib.class})
//@ExtendWith(MockitoExtension.class)
//@WebMvcTest(BookController.class)
//public class BookControllerTest {
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private BookService bookService;
//
//    @MockitoBean
//    private ElasticsearchClient elasticsearchClient;
//
//    @InjectMocks
//    private BookController bookController;
//
//
//
//    private Book book;
//    private UUID bookId;
//    private UUID userId;
//    private String token;
//    private SessionResponse mockSessionResponse;
//
//    @BeforeEach
//    void setUp() {
//        userId = UUID.fromString("73fded46-c09b-49cf-b581-8ed145a887fe");
//        token = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiYWRtaW4iLCJjYXJ0SWQiOiJlZjZlMjk4Ny03ZGU0LTQ1NzAtYTBhYS01MDgwZDRhNDdmYTEiLCJ1c2VySWQiOiJhMzk4M2IzNi02MDk0LTRlZWEtYmQzNy0yOTdmOGFlZTMwNzMiLCJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJ1c2VybmFtZSI6InRlc3R1c2VyIiwic3ViIjoidGVzdEBleGFtcGxlLmNvbSIsImlhdCI6MTc0MjIzNDkyNiwiZXhwIjoxNzQyMzIxMzI2fQ.1nJBH-ei2BCs7HOUJmCnu1-wbQhRfij2qfbBYbTZFok";
//
//        bookId = UUID.randomUUID();
//        book = Book.builder()
//                .id(bookId)
//                .title("Test Book")
//                .author("Test Author")
//                .coverImageUrl("https://www.hostinger.co.uk/tutorials/wp-content/uploads/sites/2/2022/07/the-structure-of-a-url.png")
//                .bannerImageUrl("https://www.hostinger.co.uk/tutorials/wp-content/uploads/sites/2/2022/07/the-structure-of-a-url.png")
//                .price(new BigDecimal("19.99"))
//                .description("A test book")
//                .year(2024)
//                .genre("Fiction")
//                .quantity(10)
//                .build();
//
//        mockSessionResponse = new SessionResponse("user1@example.com", "user1", "123e4567-e89b-12d3-a456-426614174000", "USER", "cart456");
//    }
//
//
//    @Test
//    void getBooks_ShouldReturnBooksList() throws Exception {
//        when(bookService.getAllBooks()).thenReturn(List.of(book));
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/api/books")
//                        .header("Authorization", "Bearer " + token)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].title").value("Test Book"));
//    }
//
//    @Test
//    void getBook_ShouldReturnBook_WhenExists() throws Exception {
//        when(bookService.getBookById(bookId)).thenReturn(book);
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{bookId}", bookId.toString())
//                        .header("Authorization", "Bearer " + token)
//                        .contentType(MediaType.APPLICATION_JSON)
//                )
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.title").value("Test Book"));
//    }
//
//    @Test
//    void getBook_ShouldReturnNotFound_WhenBookDoesNotExist() throws Exception {
//        when(bookService.getBookById(bookId)).thenReturn(null);
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{bookId}", bookId.toString())
//                        .header("Authorization", "Bearer " + token)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void createBook_ShouldReturnCreatedBook() throws Exception {
//        BookData bookData = new BookData("Test Book", "Test Author", "AuthorGuy", "2024", "24", "32", "10");
//
//        Book book = Book.builder()
//                .id(bookId)
//                .title("Test Book")
//                .author("Test Author")
//                .coverImageUrl("https://www.hostinger.co.uk/tutorials/wp-content/uploads/sites/2/2022/07/the-structure-of-a-url.png")
//                .bannerImageUrl("https://www.hostinger.co.uk/tutorials/wp-content/uploads/sites/2/2022/07/the-structure-of-a-url.png")
//                .price(new BigDecimal("19.99"))
//                .description("A test book")
//                .year(2024)
//                .genre("Fiction")
//                .quantity(10)
//                .build();
//
//        when(bookService.createBook(any(BookData.class), any(File.class), any(File.class))).thenReturn(book);
//
//        File bannerImage = mock(File.class);
//        File coverImage = mock(File.class);
//        when(coverImage.getName()).thenReturn("cover.jpg");
//        when(bannerImage.getName()).thenReturn("banner.jpg");
//
//        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/books/create")
//                        .file("bannerImage", "bannerImageContent".getBytes())
//                        .file("coverImage", "coverImageContent".getBytes())
//                        .param("bookData", objectMapper.writeValueAsString(bookData))
//                        .header("Authorization", "Bearer " + token)
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isCreated());
//    }
//
//    @Test
//    void getNewest_ShouldReturnNewestBooks() throws Exception {
//        Book book = Book.builder()
//                .id(bookId)
//                .title("Newest Book")
//                .author("Newest Author")
//                .coverImageUrl("https://www.hostinger.co.uk/tutorials/wp-content/uploads/sites/2/2022/07/the-structure-of-a-url.png")
//                .bannerImageUrl("https://www.hostinger.co.uk/tutorials/wp-content/uploads/sites/2/2022/07/the-structure-of-a-url.png")
//                .price(new BigDecimal("19.99"))
//                .description("A newest book")
//                .year(2024)
//                .genre("Fiction")
//                .quantity(10)
//                .build();
//
//        when(bookService.getNewestBooks(any(Integer.class))).thenReturn(List.of(book));
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/newest")
//                        .param("year", "2024")
//                        .header("Authorization", "Bearer " + token)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].title").value("Newest Book"));
//    }
//
//    @Test
//    void searchBooks_ShouldReturnBooks_WhenQueryMatches() throws Exception {
//        when(elasticsearchClient.search(any(SearchRequest.class), eq(Book.class))).thenReturn(null);
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/books/search")
//                        .param("query", "Test")
//                        .header("Authorization", "Bearer " + token)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isInternalServerError());
//    }
//
//
//}
