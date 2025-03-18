package skytales.Payments.config;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import skytales.Payments.repository.PaymentRepository;

@Configuration
@Import({KafkaAutoConfiguration.class})
public class TestConfig {

    @Bean
    @Primary
    public PaymentRepository cartRepository() {
        return Mockito.mock(PaymentRepository.class);
    }

}
