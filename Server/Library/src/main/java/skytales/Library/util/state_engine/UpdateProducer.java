package skytales.Library.util.state_engine;

import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import skytales.Library.util.state_engine.dto.BookMessage;
import skytales.Library.util.state_engine.model.UpdateType;
import skytales.Library.util.state_engine.utils.KafkaMessage;


@Service
public class UpdateProducer {


    private final KafkaTemplate<String, KafkaMessage<?>> kafkaTemplate;

    public UpdateProducer(KafkaTemplate<String, KafkaMessage<?>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendBookUpdate(UpdateType updateType, Object book) {
        BookMessage bookMessage = createBookMessageFromBook(book);

        KafkaMessage<BookMessage> request = new KafkaMessage<>(bookMessage);
        request.setType(updateType.toString());

        kafkaTemplate.send("book-updates", request);

    }

    private BookMessage createBookMessageFromBook(Object book) {
        ModelMapper modelMapper = new ModelMapper();
        try {
            return modelMapper.map(book, BookMessage.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error mapping Book to BookMessage: " + e.getMessage(), e);
        }
    }


}