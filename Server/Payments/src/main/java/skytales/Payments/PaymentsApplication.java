package skytales.Payments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"skytales.Payments",
		"skytales.common"
})
public class PaymentsApplication {
	public static void main(String[] args) {
		SpringApplication.run(PaymentsApplication.class, args);
	}

}
