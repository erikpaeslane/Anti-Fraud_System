package antifraud.exception.transaction;

public class WrongRegionFormatException extends IllegalArgumentException {

    String message;

    public WrongRegionFormatException(String message) {
        this.message = message;
    }
}
