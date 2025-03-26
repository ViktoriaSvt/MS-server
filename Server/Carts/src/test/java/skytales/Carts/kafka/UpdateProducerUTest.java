package skytales.Carts.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import skytales.Carts.util.state_engine.UpdateProducer;
import skytales.Carts.util.state_engine.model.KafkaMessage;


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
    public void testSendBatchSyncRequest() {
        updateProducer.sendBatchSyncRequest();

        verify(kafkaTemplate, times(1)).send(eq("sync-db"), kafkaMessageCaptor.capture());
        KafkaMessage<?> capturedMessage = kafkaMessageCaptor.getValue();

        assertEquals("", capturedMessage.getData());
    }

    @Test
    public void testSendRedisSyncRequest() {
        updateProducer.sendRedisSyncRequest();

        verify(kafkaTemplate, times(1)).send(eq("sync-redis-latestInf"), kafkaMessageCaptor.capture());
        KafkaMessage<?> capturedMessage = kafkaMessageCaptor.getValue();

        assertEquals("", capturedMessage.getData());
    }
}