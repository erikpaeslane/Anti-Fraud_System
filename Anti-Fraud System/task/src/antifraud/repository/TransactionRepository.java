package antifraud.repository;

import antifraud.entity.Transaction;
import antifraud.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t.ipAddress FROM Transaction t WHERE t.cardNumber = :cardNumber AND " +
            "t.date BETWEEN :created1 AND :created2")
    List<String> findAllIpAddressesByCreatedDateAfter(@Param("cardNumber") String cardNumber,
                                                      @Param("created1") LocalDateTime startDate,
                                                      @Param("created2") LocalDateTime endDate);
    @Query("SELECT t.region FROM Transaction t WHERE t.cardNumber = :cardNumber AND " +
            "t.date BETWEEN :created1 AND :created2")
    List<Region> findAllRegionsByCardNumberAndCreatedDateAfter(@Param("cardNumber") String cardNumber,
                                                               @Param("created1") LocalDateTime startDate,
                                                               @Param("created2") LocalDateTime endDate);
    @Query("SELECT t FROM Transaction t WHERE t.cardNumber = :cardNumber AND " +
            "t.date BETWEEN :created1 AND :created2")
    List<Transaction> findAllByCardNumberAndCreatedAfter(@Param("cardNumber") String cardNumber,
                                                         @Param("created1") LocalDateTime startDate,
                                                         @Param("created2") LocalDateTime endDate);
    @Query("SELECT t FROM Transaction t")
    List<Transaction> findAllTransactions();

    List<Transaction> findAllByCardNumber(String cardNumber);

    boolean existsByCardNumber(String cardNumber);

    Transaction findTransactionById(long id);
}
