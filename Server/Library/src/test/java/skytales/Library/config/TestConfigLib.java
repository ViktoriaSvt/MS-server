package skytales.Library.config;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import skytales.Library.repository.BookRepository;


@Configuration
@Import({KafkaAutoConfiguration.class})
public class TestConfigLib {

    @Bean
    @Primary
    public BookRepository cartRepository() {
        return Mockito.mock(BookRepository.class);
    }

}
