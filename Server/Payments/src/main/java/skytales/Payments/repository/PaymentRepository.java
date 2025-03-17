package skytales.Payments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skytales.Payments.model.Payment;


import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findTop4ByUserOrderByCreatedAtDesc(UUID customerId);

    List<Payment> findByUser(UUID userId);
}
