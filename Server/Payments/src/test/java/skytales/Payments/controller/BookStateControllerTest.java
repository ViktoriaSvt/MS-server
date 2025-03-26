package skytales.Payments.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import skytales.Payments.config.TestConfig;
import skytales.Payments.model.BookState;
import skytales.Payments.service.PaymentService;
import skytales.Payments.util.config.security.SecurityConfig;
import skytales.Payments.web.BookStateController;
import skytales.Payments.web.dto.StockChange;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({SecurityConfig.class, TestConfig.class})
@ExtendWith(SpringExtension.class)
@WebMvcTest(BookStateController.class)
public class BookStateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @SpyBean
    private BookState bookState;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        token = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiYWRtaW4iLCJjYXJ0SWQiOiIzYzExYzNlNi1hNzllLTQ2N2EtYWJhZi0yOGQ0OGQxZjdiM2IiLCJ1c2VySWQiOiJhMzk4M2IzNi02MDk0LTRlZWEtYmQzNy0yOTdmOGFlZTMwNzMiLCJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJ1c2VybmFtZSI6InRlc3R1c2VyIiwic3ViIjoidGVzdEBleGFtcGxlLmNvbSIsImlhdCI6MTc0MjczMjUzMSwiZXhwIjoxNzQyNzM3Nzg3fQ.eNG2LyyvCpR8DPFE6rEFWi3vUFoi5pdmXtOa8rzNOgs";
        bookId = UUID.fromString("73fded46-c09b-49cf-b581-8ed145a887fe");
    }

    @Test
    void testUpdateBookState() throws Exception {
        StockChange stockChange = new StockChange(bookId, 10);

        doNothing().when(paymentService).addBookToState(eq(bookId), eq(10));

        String stockChangeJson = objectMapper.writeValueAsString(stockChange);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/payments/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stockChangeJson)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Book state updated successfully"));

        verify(paymentService, times(1)).addBookToState(eq(bookId), eq(10));
    }

    @Test
    void testGetBookStateResponse() throws Exception {

        Map<UUID, BookState.BookDetails> bookStateMap = Map.of(
                bookId, new BookState.BookDetails(bookId, 10)
        );

        when(paymentService.getBookState()).thenReturn(bookStateMap);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/payments/check_local_state")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(paymentService, times(1)).getBookState();
    }



}
