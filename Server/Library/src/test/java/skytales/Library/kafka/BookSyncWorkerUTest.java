package skytales.Library.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import skytales.Library.util.state_engine.dto.BookMessage;
import skytales.Library.util.state_engine.model.UpdateType;
import skytales.Library.util.state_engine.utils.BookSyncWorker;
import skytales.Library.util.state_engine.utils.KafkaMessage;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookSyncWorkerUTest {

    @InjectMocks
    private BookSyncWorker bookSyncWorker;

    @Mock
    private KafkaTemplate<UpdateType, KafkaMessage<?>> kafkaTemplate;

    @Captor
    private ArgumentCaptor<KafkaMessage<?>> kafkaMessageCaptor;

    private KafkaMessage<BookMessage> createBookMessage(UpdateType updateType) {
        BookMessage bookMessage = new BookMessage(
                UUID.randomUUID(),
                "Test Book",
                "Fiction",
                "Author",
                "http://example.com/banner.jpg",
                2023,
                new BigDecimal("19.99"),
                100
        );
        KafkaMessage<BookMessage> request = new KafkaMessage<>(bookMessage);
        request.setType(updateType.toString());
        return request;
    }

    @Test
    public void testProcessBookUpdate_NewBook() {
        KafkaMessage<BookMessage> request = createBookMessage(UpdateType.NEW_BOOK);

        bookSyncWorker.processBookUpdate(request);

        verify(kafkaTemplate, times(1)).send(eq("book-new"), kafkaMessageCaptor.capture());
        KafkaMessage<?> capturedMessage = kafkaMessageCaptor.getValue();
        assertEquals(request, capturedMessage);
    }

    @Test
    public void testProcessBookUpdate_RemoveBook() {
        KafkaMessage<BookMessage> request = createBookMessage(UpdateType.REMOVE_BOOK);

        bookSyncWorker.processBookUpdate(request);

        verify(kafkaTemplate, times(1)).send(eq("book-remove"), kafkaMessageCaptor.capture());
        KafkaMessage<?> capturedMessage = kafkaMessageCaptor.getValue();
        assertEquals(request, capturedMessage);
    }

    @Test
    public void testProcessBookUpdate_StockChange() {
        KafkaMessage<BookMessage> request = createBookMessage(UpdateType.STOCK_CHANGE);

        bookSyncWorker.processBookUpdate(request);

        verify(kafkaTemplate, times(1)).send(eq("stock-change"), kafkaMessageCaptor.capture());
        KafkaMessage<?> capturedMessage = kafkaMessageCaptor.getValue();
        assertEquals(request, capturedMessage);
    }

    @Test
    public void testProcessBookUpdate_UnknownType() {
        KafkaMessage<BookMessage> request = createBookMessage(UpdateType.NEW_BOOK);
        request.setType("UNKNOWN_TYPE");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bookSyncWorker.processBookUpdate(request);
        });

        assertEquals("Unknown update type", exception.getMessage());
    }
}
