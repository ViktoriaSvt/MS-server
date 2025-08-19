package skytales.Library.util.state_engine.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import skytales.Library.util.state_engine.dto.BookMessage;


public class MessageDeserializer implements Deserializer<KafkaMessage<?>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public KafkaMessage<?> deserialize(String topic, byte[] data) {
        try {
            JsonNode root = objectMapper.readTree(data);

            KafkaMessage<Object> kafkaMessage = objectMapper.treeToValue(root, KafkaMessage.class);

            Class<?> dataClass = getClassForType(topic);
            JsonNode payloadNode = root.get("data");
            Object payload = objectMapper.treeToValue(payloadNode, dataClass);

            kafkaMessage.setData(payload);
            return kafkaMessage;

        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize message", e);
        }
    }



    private Class<?> getClassForType(String type) {
        return switch (type) {
            case "book-new", "book-stock-update", "book-remove", "book-updates" -> BookMessage.class;
            default -> String.class;
        };
    }
}





