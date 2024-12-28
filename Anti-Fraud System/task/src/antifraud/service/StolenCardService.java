package antifraud.service;

import antifraud.entity.StolenCard;
import antifraud.repository.StolenCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class StolenCardService {

    private final StolenCardRepository stolenCardRepository;
    private static final String STOLEN_CARD_FORMAT = "^(4[0-9]{3})([0-9]{4})([0-9]{4})([0-9]{4})$";

    @Autowired
    public StolenCardService(StolenCardRepository stolenCardRepository) {
        this.stolenCardRepository = stolenCardRepository;
    }

    public ResponseEntity<?> addStolenCard(String stolenCardNumber) {
        if (isCardNumberNotValid(stolenCardNumber))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        if (stolenCardRepository.existsStolenCardByNumber(stolenCardNumber))
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        StolenCard stolenCard = new StolenCard(stolenCardNumber);
        stolenCardRepository.save(stolenCard);
        return ResponseEntity.status(HttpStatus.OK).body(stolenCard);
    }

    public ResponseEntity<?> getaAllStolenCards() {
        return ResponseEntity.status(HttpStatus.OK).body(stolenCardRepository.findAllByOrderByIdAsc());
    }

    public ResponseEntity<?> removeStolenCard(String stolenCardNumber) {
        if (isCardNumberNotValid(stolenCardNumber))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        Optional<StolenCard> stolenCard = stolenCardRepository.findStolenCardByNumber(stolenCardNumber);
        if(stolenCard.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        stolenCardRepository.delete(stolenCard.get());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("status", ("Card " + stolenCardNumber + " successfully removed!")));
    }

    public boolean isCardNumberExist(String cardNumber) {
        return stolenCardRepository.existsStolenCardByNumber(cardNumber);
    }

    public boolean isCardNumberNotValid(String cardNumber) {
        if (cardNumber == null)
            return true;
        return !cardNumber.matches(STOLEN_CARD_FORMAT) || !isValidLuhn(cardNumber);
    }

    private boolean isValidLuhn(String cardNumber) {
        boolean isSecond = false;
        int sum = 0;
        for (int i = cardNumber.length()-1; i >= 0; i--) {
            int digit = cardNumber.charAt(i) - '0';
            if (isSecond)
                digit *= 2;
            sum += digit / 10;
            sum += digit % 10;
            isSecond = !isSecond;
        }
        return sum % 10 == 0;
    }
}
