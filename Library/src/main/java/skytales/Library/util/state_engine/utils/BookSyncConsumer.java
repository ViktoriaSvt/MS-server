package skytales.Library.state_engine.utils;


import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import skytales.Library.state_engine.dto.BookMessage;


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
