package antifraud.repository;

import antifraud.entity.StolenCard;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StolenCardRepository extends CrudRepository<StolenCard, Long> {
    Optional<StolenCard> findStolenCardByNumber(String number);
    boolean existsStolenCardByNumber(String number);
    List<StolenCard> findAllByOrderByIdAsc();
}
