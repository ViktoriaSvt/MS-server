package skytales.Payments.util.state_engine;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import skytales.Payments.util.state_engine.utils.KafkaMessage;

@Service
public class UpdateProducer {

    private final KafkaTemplate<String, KafkaMessage<?>> kafkaTemplate;

    public UpdateProducer(KafkaTemplate<String, KafkaMessage<?>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void clearCartForUser(String id) {

        KafkaMessage<String> request = new KafkaMessage<>(id);
        kafkaTemplate.send("cart-checkout", request);
    }
}