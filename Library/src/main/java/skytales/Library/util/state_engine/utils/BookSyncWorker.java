package skytales.Library.state_engine.utils;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import skytales.Library.state_engine.dto.BookMessage;
import skytales.Library.state_engine.model.UpdateType;

@Service
public class BookSyncWorker {

    private final KafkaTemplate<UpdateType, KafkaMessage<?>> kafkaTemplate;

    public BookSyncWorker(KafkaTemplate<UpdateType, KafkaMessage<?>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processBookUpdate(KafkaMessage<BookMessage> request) {

        UpdateType type;
        try {
            type = UpdateType.valueOf(request.getType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown update type");
        }

        switch (type) {
            case NEW_BOOK:
                kafkaTemplate.send("book-new", request);
                break;
            case REMOVE_BOOK:
                kafkaTemplate.send("book-remove", request);
                break;
            case STOCK_CHANGE:
                kafkaTemplate.send("stock-change", request);
                break;
            default:
                throw new IllegalArgumentException("Unknown update type");
        }
    }
}
