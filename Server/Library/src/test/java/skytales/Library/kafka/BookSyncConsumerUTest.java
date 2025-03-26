package skytales.Library.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import skytales.Library.util.state_engine.dto.BookMessage;
import skytales.Library.util.state_engine.utils.BookSyncConsumer;
import skytales.Library.util.state_engine.utils.BookSyncWorker;
import skytales.Library.util.state_engine.utils.KafkaMessage;


import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BookSyncConsumerUTest {

    @InjectMocks
    private BookSyncConsumer bookSyncConsumer;

    @Mock
    private BookSyncWorker bookSyncWorker;

    private KafkaMessage<BookMessage> createBookMessage() {
        BookMessage bookMessage = new BookMessage(
                UUID.randomUUID(),
                "Test Book",
                "Fiction",
                "Author",
                "http://example.com/cover.jpg",
                2023,
                new BigDecimal("19.99"),
                100
        );
        KafkaMessage<BookMessage> request = new KafkaMessage<>(bookMessage);
        request.setType("NEW_BOOK");
        return request;
    }

    @Test
    public void testConsumeBookUpdate() {
        KafkaMessage<BookMessage> message = createBookMessage();

        bookSyncConsumer.consumeBookUpdate(message);

        verify(bookSyncWorker, times(1)).processBookUpdate(message);
    }
}