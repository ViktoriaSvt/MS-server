package skytales.Payments.util;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import skytales.Payments.service.PaymentService;
import skytales.Payments.util.state_engine.dto.BookMessage;
import skytales.Payments.util.state_engine.transferModel.KafkaMessage;

@Service
public class StockEvents {


    private final PaymentService paymentService;

    public StockEvents(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "book-new", groupId = "book-sync-payment")
    public void handleNewBook(KafkaMessage<?> message) {
        paymentService.addBookToState((BookMessage) message.getData());

    }
}
