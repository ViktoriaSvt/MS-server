package skytales.payment.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import skytales.payment.model.BookState;

@Configuration
public class BookStateConfig {

    @Bean
    @Scope("singleton")
    public BookState bookState() {
        return new BookState();
    }
}

