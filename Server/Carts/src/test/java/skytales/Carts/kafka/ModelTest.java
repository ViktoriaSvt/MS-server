package skytales.Carts.kafka;

import org.junit.jupiter.api.Test;
import skytales.Carts.util.state_engine.model.KafkaMessage;
import skytales.Carts.util.state_engine.model.UpdateType;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelTest {

    @Test
    public void testKafkaMessageConstructor() {
        String type = "book-new";
        String data = "Test data";

        KafkaMessage<String> kafkaMessage = new KafkaMessage<>(type, data);

        assertEquals(type, kafkaMessage.getType());
        assertEquals(data, kafkaMessage.getData());
    }

    @Test
    public void testEnumValues() {
        assertEquals(UpdateType.NEW_BOOK, UpdateType.valueOf("NEW_BOOK"));
        assertEquals(UpdateType.REMOVE_BOOK, UpdateType.valueOf("REMOVE_BOOK"));
        assertEquals(UpdateType.STOCK_CHANGE, UpdateType.valueOf("STOCK_CHANGE"));
    }

    @Test
    public void testEnumOrdinal() {
        assertEquals(0, UpdateType.NEW_BOOK.ordinal());
        assertEquals(1, UpdateType.REMOVE_BOOK.ordinal());
        assertEquals(2, UpdateType.STOCK_CHANGE.ordinal());
    }
}
