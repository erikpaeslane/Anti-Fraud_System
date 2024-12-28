package antifraud.model;

import java.time.LocalDateTime;

public record TransactionValidationResult (long amount, String ip, String number,
                                           Region region, LocalDateTime date, TransactionStatus status, String info){
}
