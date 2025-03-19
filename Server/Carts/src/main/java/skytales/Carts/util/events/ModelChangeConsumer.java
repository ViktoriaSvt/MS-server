package skytales.Carts.util.events;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import skytales.Carts.web.dto.BookRequest;
import skytales.Carts.service.BookReferenceService;
import skytales.Carts.util.state_engine.dto.BookMessage;
import skytales.Carts.util.state_engine.model.KafkaMessage;


@Service
public class ModelChangeConsumer {

    private final BookReferenceService bookReferenceService;

    public ModelChangeConsumer(BookReferenceService bookReferenceService) {
        this.bookReferenceService = bookReferenceService;
    }

    @KafkaListener(topics = "book-new", groupId = "book-sync")
    public void handleNewBook(KafkaMessage<?> message) {
        bookReferenceService.addBookToState((BookMessage) message.getData());
    }

    @KafkaListener(topics = "book-remove", groupId = "book-sync")
    public void handleRemoveBook(KafkaMessage<?> message) {
        bookReferenceService.removeBookFromState((BookMessage) message.getData());
    }

}
