package skytales.Payments.dto;

import java.util.List;

public record PaymentRequest(
        String paymentMethodId,
        Long amount,
        List<BookItem> books
) {

}
