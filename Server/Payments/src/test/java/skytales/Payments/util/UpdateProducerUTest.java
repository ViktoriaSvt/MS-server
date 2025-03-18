package skytales.Payments.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import skytales.Payments.util.state_engine.UpdateProducer;
import skytales.Payments.util.state_engine.transferModel.KafkaMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateProducerUTest {

    @InjectMocks
    private UpdateProducer updateProducer;

    @Mock
    private KafkaTemplate<String, KafkaMessage<?>> kafkaTemplate;

    @Captor
    private ArgumentCaptor<KafkaMessage<?>> kafkaMessageCaptor;

    @Test
    public void testClearCartForUser() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";

        updateProducer.clearCartForUser(userId);

        verify(kafkaTemplate, times(1)).send(eq("cart-checkout"), kafkaMessageCaptor.capture());
        KafkaMessage<?> capturedMessage = kafkaMessageCaptor.getValue();

        assertEquals(userId, capturedMessage.getData());
    }
}