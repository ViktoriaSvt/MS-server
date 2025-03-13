package com.skytales.common.common_utils.common.state_engine;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.skytales.common.common_utils.common.state_engine.dto.BookMessage;
import com.skytales.common.common_utils.common.state_engine.model.UpdateType;
import com.skytales.common.common_utils.common.state_engine.utils.KafkaMessage;

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
