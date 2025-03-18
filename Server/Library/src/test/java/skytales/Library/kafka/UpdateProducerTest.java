package skytales.Library.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import skytales.Library.model.Book;
import skytales.Library.util.state_engine.UpdateProducer;
import skytales.Library.util.state_engine.dto.BookMessage;
import skytales.Library.util.state_engine.model.UpdateType;
import skytales.Library.util.state_engine.utils.KafkaMessage;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateProducerTest {

    @InjectMocks
    private UpdateProducer updateProducer;

    @Mock
    private KafkaTemplate<String, KafkaMessage<?>> kafkaTemplate;

    @Captor
    private ArgumentCaptor<KafkaMessage<?>> kafkaMessageCaptor;


    @Test
    public void testSendBookUpdate() {
        Book book = Book.builder()
                .id(UUID.randomUUID())
                .title("The Great Adventure")
                .genre("Adventure")
                .author("John Doe")
                .bannerImageUrl("http://example.com/banner.jpg")
                .coverImageUrl("http://example.com/cover.jpg")
                .year(2023)
                .price(new BigDecimal("19.99"))
                .description("An epic adventure story that takes you through uncharted territories.")
                .quantity(100)
                .build();

        UpdateType updateType = UpdateType.NEW_BOOK;

        updateProducer.sendBookUpdate(updateType, book);

        verify(kafkaTemplate, times(1)).send(eq("book-updates"), kafkaMessageCaptor.capture());
        KafkaMessage<?> capturedMessage = kafkaMessageCaptor.getValue();

        assertInstanceOf(BookMessage.class, capturedMessage.getData());
        BookMessage bookMessage = (BookMessage) capturedMessage.getData();
        assertEquals(book.getId(), bookMessage.id());
        assertEquals("The Great Adventure", bookMessage.title());
        assertEquals("Adventure", bookMessage.genre());
        assertEquals("John Doe", bookMessage.author());
        assertEquals("http://example.com/cover.jpg", bookMessage.coverImageUrl());
        assertEquals(2023, bookMessage.year());
        assertEquals(BigDecimal.valueOf(19.99), bookMessage.price());
        assertEquals(100, bookMessage.quantity());
        assertEquals(updateType.toString(), capturedMessage.getType());
    }



}