package skytales.Carts.kafka.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import skytales.Carts.util.state_engine.MessageDeserializer;
import skytales.Carts.util.state_engine.dto.BookMessage;
import skytales.Carts.util.state_engine.model.KafkaMessage;
import skytales.Carts.web.dto.BookRequest;


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
    public void testDeserialize_BookNew() throws Exception {
        BookRequest bookRequest = new BookRequest(UUID.randomUUID(), "Test Book", "Fiction", "Author", "http://example.com/banner.jpg", 2023, new BigDecimal("19.99"), 100);
        KafkaMessage<BookRequest> kafkaMessage = new KafkaMessage<>(bookRequest);
        kafkaMessage.setType("book-new");

        byte[] data = objectMapper.writeValueAsBytes(kafkaMessage);

        KafkaMessage<?> result = messageDeserializer.deserialize("book-new", data);

        assertInstanceOf(BookMessage.class, result.getData());
        BookMessage resultData = (BookMessage) result.getData();
        assertEquals(bookRequest.id(), resultData.id());
        assertEquals(bookRequest.title(), resultData.title());
        assertEquals(bookRequest.genre(), resultData.genre());
        assertEquals(bookRequest.author(), resultData.author());
        assertEquals(bookRequest.coverImageUrl(), resultData.coverImageUrl());
        assertEquals(bookRequest.year(), resultData.year());
        assertEquals(bookRequest.price(), resultData.price());
        assertEquals(bookRequest.quantity(), resultData.quantity());
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

    @Test
    public void testDeserialize_InvalidData() {
        byte[] data = "invalid data".getBytes();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            messageDeserializer.deserialize("book-new", data);
        });

        assertEquals("Failed to deserialize message", exception.getMessage());
    }

    @Test
    public void testGetClassForType_Default() {
        Class<?> result = messageDeserializer.getClassForType("unknown-type");

        assertEquals(String.class, result, "Default type should return String.class");
    }
}
