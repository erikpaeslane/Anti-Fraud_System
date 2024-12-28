package antifraud.exception.user;

public class InvalidNameException extends UserValidationException {
    public InvalidNameException(String message) {
        super(message);
    }
}
