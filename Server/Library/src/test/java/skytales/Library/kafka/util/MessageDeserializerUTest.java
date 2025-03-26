package skytales.Library.kafka.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import skytales.Library.util.state_engine.dto.BookMessage;
import skytales.Library.util.state_engine.utils.KafkaMessage;
import skytales.Library.util.state_engine.utils.MessageDeserializer;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MessageDeserializerUTest {

    private MessageDeserializer messageDeserializer;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        messageDeserializer = new MessageDeserializer();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testDeserialize_InvalidData() {
        byte[] data = "invalid data".getBytes();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            messageDeserializer.deserialize("book-new", data);
        });

        assertEquals("Failed to deserialize message", exception.getMessage());
    }


    @Test
    public void testDeserialize_BookUpdates() throws Exception {
        BookMessage bookMessage = new BookMessage(UUID.randomUUID(), "Test Book", "Fiction", "Author", "http://example.com/banner.jpg", 2023, new BigDecimal("19.99"), 100);
        KafkaMessage<BookMessage> kafkaMessage = new KafkaMessage<>(bookMessage);
        kafkaMessage.setType("book-updates");

        byte[] data = objectMapper.writeValueAsBytes(kafkaMessage);

        KafkaMessage<?> result = messageDeserializer.deserialize("book-updates", data);

        assertInstanceOf(BookMessage.class, result.getData());
        BookMessage resultData = (BookMessage) result.getData();
        assertEquals(bookMessage.id(), resultData.id());
        assertEquals(bookMessage.title(), resultData.title());
        assertEquals(bookMessage.genre(), resultData.genre());
        assertEquals(bookMessage.author(), resultData.author());
        assertEquals(bookMessage.coverImageUrl(), resultData.coverImageUrl());
        assertEquals(bookMessage.year(), resultData.year());
        assertEquals(bookMessage.price(), resultData.price());
        assertEquals(bookMessage.quantity(), resultData.quantity());
    }

}
