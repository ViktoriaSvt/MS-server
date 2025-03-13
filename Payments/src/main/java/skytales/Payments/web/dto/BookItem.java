package skytales.Payments.dto;

import java.math.BigDecimal;

public record BookItem(
        String bookId,
        String title,
        BigDecimal price
) {}