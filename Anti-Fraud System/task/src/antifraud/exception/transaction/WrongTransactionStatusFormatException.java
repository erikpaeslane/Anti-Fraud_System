package antifraud.exception.transaction;

public class WrongTransactionStatusFormatException extends IllegalArgumentException {

    String message;

    public WrongTransactionStatusFormatException(String message) {
        this.message = message;
    }
}
