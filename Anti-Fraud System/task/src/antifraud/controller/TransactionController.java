package antifraud.controller;

import antifraud.model.requests.TransactionCredentials;
import antifraud.model.requests.TransactionUpdateCredentials;
import antifraud.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/api/antifraud/transaction")
    public ResponseEntity<?> addTransaction(@RequestBody TransactionCredentials transactionCredentials) {
        return transactionService.createTransaction(transactionCredentials);
    }

    @GetMapping("/api/antifraud/history")
    public ResponseEntity<?> getTransactionsHistory() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/api/antifraud/history/{number}")
    public ResponseEntity<?> getTransactionsHistory(@PathVariable String number) {
        System.out.println("Card: " + number);
        return transactionService.getTransactionByCardNumber(number);
    }

    @PutMapping("/api/antifraud/transaction")
    public ResponseEntity<?> updateTransaction(@RequestBody TransactionUpdateCredentials transactionUpdateCredentials) {
        return transactionService.updateTransaction(transactionUpdateCredentials);
    }






}
