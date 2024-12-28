package antifraud.model;

import antifraud.entity.Transaction;

import java.time.LocalDateTime;

public record TransactionResponse (
        long transactionId,
        long amount,
        String ip,
        String number,
        Region region,
        LocalDateTime date,
        String result,
        String feedback
    ) {

    // Factory method for flexibility
    public static TransactionResponse fromTransaction(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getIpAddress(),
                transaction.getCardNumber(),
                transaction.getRegion(),
                transaction.getDate(),
                defaultIfNull(transaction.getStatus()),
                defaultIfNull(transaction.getFeedback())
        );
    }

    private static String defaultIfNull(Object object) {
        return object == null ? "" : object.toString();
    }
}
