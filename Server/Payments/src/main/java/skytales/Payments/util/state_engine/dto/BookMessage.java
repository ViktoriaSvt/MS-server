package skytales.Payments.util.state_engine.dto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record BookMessage(
        UUID id,
        String title,
        String genre,
        String author,
        String coverImageUrl,
        int year,
        BigDecimal price,
        int quantity
) implements Serializable {
}
