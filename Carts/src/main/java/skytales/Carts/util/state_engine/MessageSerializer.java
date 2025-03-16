package skytales.Carts.util.state_engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import skytales.Carts.util.state_engine.model.KafkaMessage;


public class MessageSerializer implements Serializer<KafkaMessage<?>> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, KafkaMessage<?> kafkaMessage) {
        try {
            return objectMapper.writeValueAsBytes(kafkaMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize message", e);
        }
    }
}
