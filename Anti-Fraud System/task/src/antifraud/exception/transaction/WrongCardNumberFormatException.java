package antifraud.exception.transaction;

public class WrongCardNumberFormatException extends IllegalArgumentException {

    String message;

    public WrongCardNumberFormatException(String message) {
        this.message = message;
    }
}
