package skytales.Carts.config;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.*;
import skytales.Carts.repository.CartRepository;

@Configuration
@Import({KafkaAutoConfiguration.class})
public class TestConfig {

    @Bean
    @Primary
    public CartRepository cartRepository() {
        return Mockito.mock(CartRepository.class);
    }

}
