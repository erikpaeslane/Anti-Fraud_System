package antifraud.model.requests;

public record TransactionCredentials(long amount, String ip, String number,
                                     String region, String date) {
}
