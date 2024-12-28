package antifraud.model.requests;

public record TransactionUpdateCredentials (long transactionId, String feedback) {
}
