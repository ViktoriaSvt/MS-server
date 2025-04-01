package skytales.Payments.web.dto;

import java.math.BigDecimal;

public record BookItem(
        String id,
        String title,
        BigDecimal price
) {}