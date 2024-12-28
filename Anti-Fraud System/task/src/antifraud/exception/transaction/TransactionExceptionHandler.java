package antifraud.exception.transaction;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class TransactionExceptionHandler {

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<?> handleInvalidAmountException(InvalidAmountException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(WrongDateTimeFormatException.class)
    public ResponseEntity<?> handleWrongDateFormatException(WrongDateTimeFormatException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(WrongCardNumberFormatException.class)
    public ResponseEntity<?> handleWrongCardNumberFormatException(WrongCardNumberFormatException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(WrongIpAddressFormatException.class)
    public ResponseEntity<?> handleWrongIpAddressFormatException(WrongIpAddressFormatException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(WrongRegionFormatException.class)
    public ResponseEntity<?> handleWrongRegionFormatException(WrongRegionFormatException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(WrongTransactionStatusFormatException.class)
    public ResponseEntity<?> handleWrongTransactionStatusFormatException(WrongTransactionStatusFormatException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
