package skytales.Carts.redis.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import skytales.Carts.service.BookReferenceService;
import skytales.Carts.util.events.ModelChangeConsumer;
import skytales.Carts.util.state_engine.dto.BookMessage;
import skytales.Carts.util.state_engine.model.KafkaMessage;
import skytales.Carts.web.dto.BookRequest;


import java.math.BigDecimal;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class ModelChangeConsumerUTest {

    @Mock
    private BookReferenceService bookReferenceService;

    @InjectMocks
    private ModelChangeConsumer modelChangeConsumer;

    private KafkaMessage<BookRequest> kafkaMessage;
    private BookRequest bookRequest;
    private BookMessage bookMessage;

    @BeforeEach
    void setUp() {
        bookRequest = new BookRequest(UUID.randomUUID(), "Title", "Fantasy", "Author", "http://example.com/cover.jpg", 2000, BigDecimal.valueOf(1), 30);
        bookMessage= new BookMessage(UUID.randomUUID(), "Title", "Fantasy", "Author", "http://example.com/cover.jpg", 2000, BigDecimal.valueOf(1), 30);
        kafkaMessage = new KafkaMessage<>();
        kafkaMessage.setData(bookRequest);
    }

//    @Test
//    void handleNewBook() {
//        modelChangeConsumer.handleNewBook(kafkaMessage);
//
//        verify(bookReferenceService, times(1)).addBookToState(bookRequest);
//    }
//
//    @Test
//    void handleRemoveBook() {
//        modelChangeConsumer.handleRemoveBook(kafkaMessage);
//
//        verify(bookReferenceService, times(1)).removeBookFromState(bookRequest);
//    }
//
//    @Test
//    void handleStockUpdate() {
//        modelChangeConsumer.handleStockUpdate(bookMessage);
//        verify(bookReferenceService, times(1)).updateBookStock(bookMessage);
//    }
}
