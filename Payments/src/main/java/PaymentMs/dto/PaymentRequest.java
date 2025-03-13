package skytales.payment.dto;

import java.math.BigDecimal;
import java.util.List;

public record PaymentRequest(
        String paymentMethodId,
        Long amount,
        List<BookItem> books
) {

}
