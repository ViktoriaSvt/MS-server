package skytales.Carts.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookRequest(
        UUID id,
        String title,
        String genre,
        String author,
        String coverImageUrl,
        int year,
        BigDecimal price,
        int quantity
) {
}
