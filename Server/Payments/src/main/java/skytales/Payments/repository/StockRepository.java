package skytales.Payments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import skytales.Payments.model.Stock;

import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {
    Stock findByBookId(UUID bookId);
}
