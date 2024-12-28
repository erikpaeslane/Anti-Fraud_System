package antifraud.logic;

import antifraud.entity.Transaction;
import antifraud.model.TransactionValidationResult;
import org.springframework.stereotype.Component;

@Component
public class TransactionFactory {

    public static Transaction fromValidationResult(TransactionValidationResult result) {
        return Transaction.builder()
                .amount(result.amount())
                .ipAddress(result.ip())
                .cardNumber(result.number())
                .region(result.region())
                .date(result.date())
                .status(result.status())
                .info(result.info())
                .feedback(null)
                .build();
    }

}
