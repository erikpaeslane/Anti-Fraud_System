package antifraud.exception.transaction;

public class WrongIpAddressFormatException extends IllegalArgumentException {

    String message;

    public WrongIpAddressFormatException(String message) {
        this.message = message;
    }
}
