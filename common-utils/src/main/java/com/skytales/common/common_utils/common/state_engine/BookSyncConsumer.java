package skytales.common.state_engine;

import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import skytales.common.state_engine.dto.BookMessage;
import skytales.common.state_engine.utils.KafkaMessage;


@Service
@EnableKafka
public class BookSyncConsumer {

    private final BookSyncWorker bookSyncWorker;

    public BookSyncConsumer(BookSyncWorker bookSyncWorker) {
        this.bookSyncWorker = bookSyncWorker;
    }

    @KafkaListener(topics = "book-updates", groupId = "book-sync")
    public void consumeBookUpdate(KafkaMessage<BookMessage> message) {
        bookSyncWorker.processBookUpdate(message);
    }
}
