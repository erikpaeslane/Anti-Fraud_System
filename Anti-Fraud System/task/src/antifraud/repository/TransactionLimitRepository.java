package antifraud.repository;

import antifraud.entity.TransactionLimit;
import antifraud.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionLimitRepository extends JpaRepository<TransactionLimit, Long> {
    @Query("SELECT tl FROM TransactionLimit tl WHERE tl.status = :status")
    Optional<TransactionLimit> findByStatus(TransactionStatus status);
}
