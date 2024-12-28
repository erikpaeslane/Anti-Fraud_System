package antifraud.exception.transaction;

public class InvalidRegionFormatException extends IllegalArgumentException {

    String message;

    public InvalidRegionFormatException(String message) {
        this.message = message;
    }
}
