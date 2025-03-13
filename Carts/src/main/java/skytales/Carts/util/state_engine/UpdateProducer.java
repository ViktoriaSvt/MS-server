package skytales.Carts.util.state_engine;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import skytales.Carts.util.state_engine.model.KafkaMessage;

@Service
public class UpdateProducer {

    private final KafkaTemplate<String, KafkaMessage<?>> kafkaTemplate;

    public UpdateProducer(KafkaTemplate<String, KafkaMessage<?>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendBatchSyncRequest() {
        KafkaMessage<String> request = new KafkaMessage<>("");
        kafkaTemplate.send("sync-db", request);
    }

    public void sendRedisSyncRequest() {
        KafkaMessage<String> request = new KafkaMessage<>("");
        kafkaTemplate.send("sync-redis-latestInf", request);
    }
}