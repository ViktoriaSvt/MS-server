package skytales.library.model;

import jakarta.persistence.*;
import lombok.*;
import skytales.auth.model.User;
import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false)
    private String author;

    @Column(name = "banner_image_url", length = 1000)
    private String bannerImageUrl;

    @Column(name = "cover_image_url", length = 1000)
    private String coverImageUrl;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private BigDecimal price;

    @Column( columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    public int quantity;

    @ManyToOne
    private User creator;

}
