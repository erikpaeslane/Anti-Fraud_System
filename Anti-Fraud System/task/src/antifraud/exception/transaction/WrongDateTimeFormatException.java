package antifraud.exception.transaction;

public class WrongDateTimeFormatException extends IllegalArgumentException {

    private final String message;

    public WrongDateTimeFormatException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
