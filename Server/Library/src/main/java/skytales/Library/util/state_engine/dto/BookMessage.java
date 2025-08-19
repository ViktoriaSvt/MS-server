package skytales.Library.util.state_engine.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class BookMessage implements Serializable {
    private UUID id;
    private String title;
    private String genre;
    private String author;
    private String bannerImageUrl;
    private String coverImageUrl;
    private int year;
    private BigDecimal price;
    private String description;
    private int quantity;

    public BookMessage(UUID uuid, String testBook, String fiction, String author, String url, int i, BigDecimal bigDecimal, int i1) {
    }
}

