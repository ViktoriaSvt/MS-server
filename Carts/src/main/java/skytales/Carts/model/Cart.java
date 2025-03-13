package skytales.cart.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart", indexes = { @Index(name = "idx_cart_owner", columnList = "owner") })
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "cart_books",
            joinColumns = @JoinColumn(name = "cart_id", columnDefinition = "BINARY(16)"),
            inverseJoinColumns = @JoinColumn(name = "book_reference_id"))
    private Set<BookItemReference> books;

    @NotNull
    @Column(columnDefinition = "BINARY(16)")
    private UUID owner;

}