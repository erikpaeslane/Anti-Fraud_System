package antifraud.service;

import antifraud.entity.Transaction;
import antifraud.exception.transaction.*;
import antifraud.logic.TransactionFactory;
import antifraud.model.Region;
import antifraud.model.TransactionResponse;
import antifraud.model.TransactionValidationResult;
import antifraud.model.requests.TransactionCredentials;
import antifraud.model.TransactionStatus;
import antifraud.model.requests.TransactionUpdateCredentials;
import antifraud.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final IpAddressService ipAddressService;
    private final StolenCardService stolenCardService;
    private final TransactionLimitService transactionLimitService;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, IpAddressService ipAddressService,
                              StolenCardService stolenCardService, TransactionLimitService transactionLimitService) {
        this.transactionRepository = transactionRepository;
        this.ipAddressService = ipAddressService;
        this.stolenCardService = stolenCardService;
        this.transactionLimitService = transactionLimitService;
    }

    public ResponseEntity<?> createTransaction(TransactionCredentials transactionCredentials) {

        if (transactionCredentials == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        System.out.println("Validating transaction...");
        TransactionValidationResult result = validateTransaction(transactionCredentials);
        System.out.println("Convert transaction result: " + result);
        Transaction transaction = TransactionFactory.fromValidationResult(result);
        System.out.println("Saving transaction...");
        transactionRepository.save(transaction);
        return transactionToResponse(transaction);
    }

    public ResponseEntity<?> updateTransaction(TransactionUpdateCredentials transactionUpdateCredentials) {
        System.out.println("Updating transaction...");
        TransactionStatus feedbackStatus = validateTransactionStatus(transactionUpdateCredentials.feedback());
        Transaction transaction = transactionRepository.findTransactionById(transactionUpdateCredentials.transactionId());

        if (transaction == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        if (transaction.getFeedback() != null)
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        if (feedbackStatus == transaction.getStatus())
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();

        System.out.println("Transaction: ");
        System.out.println("Amount: " + transaction.getAmount());
        System.out.println("Status: " + transaction.getStatus());
        System.out.println("Info" + transaction.getInfo());
        System.out.println("Given feedback: " + feedbackStatus);

        switch (feedbackStatus) {
            case ALLOWED -> {
                transactionLimitService.increaseLimit(TransactionStatus.ALLOWED, transaction.getAmount());
                if (transaction.getStatus() == TransactionStatus.PROHIBITED)
                    transactionLimitService.increaseLimit(TransactionStatus.MANUAL_PROCESSING, transaction.getAmount());
                transaction.setFeedback(feedbackStatus);
                transactionRepository.save(transaction);
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(TransactionResponse.fromTransaction(transaction));
            }
            case MANUAL_PROCESSING -> {
                if (transaction.getStatus() == TransactionStatus.ALLOWED)
                    transactionLimitService.decreaseLimit(TransactionStatus.ALLOWED, transaction.getAmount());
                if (transaction.getStatus() == TransactionStatus.PROHIBITED)
                    transactionLimitService.increaseLimit(TransactionStatus.MANUAL_PROCESSING, transaction.getAmount());
                transaction.setFeedback(feedbackStatus);
                transactionRepository.save(transaction);
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(TransactionResponse.fromTransaction(transaction));
            }
            case PROHIBITED -> {
                transactionLimitService.decreaseLimit(TransactionStatus.MANUAL_PROCESSING, transaction.getAmount());
                if (transaction.getStatus() == TransactionStatus.ALLOWED)
                    transactionLimitService.decreaseLimit(TransactionStatus.ALLOWED, transaction.getAmount());
                transaction.setFeedback(feedbackStatus);
                transactionRepository.save(transaction);
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(TransactionResponse.fromTransaction(transaction));
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    public ResponseEntity<?> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAllTransactions();

        List<TransactionResponse> response = transactions
                .stream()
                .map(TransactionResponse::fromTransaction)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public ResponseEntity<?> getTransactionByCardNumber(String cardNumber) {
        if (stolenCardService.isCardNumberNotValid(cardNumber))
           throw new WrongCardNumberFormatException("Wrong card number");
        if (!transactionRepository.existsByCardNumber(cardNumber))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        List<Transaction> transactions = transactionRepository.findAllByCardNumber(cardNumber);
        return ResponseEntity.status(HttpStatus.OK)
                .body(transactions
                        .stream()
                        .map(TransactionResponse::fromTransaction)
                        .collect(Collectors.toList()));
    }

    public TransactionValidationResult validateTransaction(TransactionCredentials transactionCredentials) {
        List<String> prohibitionReasons = new ArrayList<>();
        LocalDateTime localDateTime = validateDate(transactionCredentials.date());
        Region region = validateRegion(transactionCredentials.region());
        TransactionStatus transactionStatus = validateAmount(transactionCredentials.amount());
        System.out.println("Validated");
        if (transactionStatus != TransactionStatus.ALLOWED)
            prohibitionReasons.add("amount");
        if (stolenCardService.isCardNumberExist(transactionCredentials.number()))
            prohibitionReasons.add("card-number");
        if (ipAddressService.existsSuspiciousIpAddress(transactionCredentials.ip()))
            prohibitionReasons.add("ip");
        TransactionStatus correlatedIpStatus = correlateIpAddressesOfCard(
                transactionCredentials.number(), transactionCredentials.ip(), localDateTime);
        TransactionStatus correlatedRegionStatus = correlateRegionOfCard(
                transactionCredentials.number(), region, localDateTime);

        if (correlatedIpStatus == TransactionStatus.MANUAL_PROCESSING &&
                transactionStatus != TransactionStatus.PROHIBITED) {
            if (transactionStatus == TransactionStatus.ALLOWED)
                transactionStatus = correlatedIpStatus;
            prohibitionReasons.add("ip-correlation");
        }
        if (correlatedIpStatus == TransactionStatus.PROHIBITED) {
            prohibitionReasons.add("ip-correlation");
            transactionStatus = TransactionStatus.PROHIBITED;
        }
        if (correlatedRegionStatus == TransactionStatus.MANUAL_PROCESSING &&
                transactionStatus != TransactionStatus.PROHIBITED) {
            if (transactionStatus == TransactionStatus.ALLOWED)
                transactionStatus = correlatedRegionStatus;
            prohibitionReasons.add("region-correlation");
        }
        if (correlatedRegionStatus == TransactionStatus.PROHIBITED){
            prohibitionReasons.add("region-correlation");
            transactionStatus = TransactionStatus.PROHIBITED;
        }
        if (prohibitionReasons.contains("ip") || prohibitionReasons.contains("card-number") ||
                correlatedIpStatus == TransactionStatus.PROHIBITED) {
            if (transactionStatus == TransactionStatus.MANUAL_PROCESSING){
                prohibitionReasons.remove("amount");
                transactionStatus = TransactionStatus.PROHIBITED;
            }
            if (correlatedIpStatus == TransactionStatus.MANUAL_PROCESSING &&
                    transactionStatus == TransactionStatus.PROHIBITED)
                prohibitionReasons.remove("correlated-ip");
            if (transactionStatus == TransactionStatus.PROHIBITED &&
                    correlatedRegionStatus == TransactionStatus.MANUAL_PROCESSING)
                prohibitionReasons.remove("correlated-region");
        }

        String reasons = prohibitionReasons.isEmpty() ? "none" : String.join(", ", prohibitionReasons);
        System.out.println("Sending validation result..");
        return new TransactionValidationResult(
                transactionCredentials.amount(),
                transactionCredentials.ip(),
                transactionCredentials.number(),
                region,
                localDateTime,
                transactionStatus,
                reasons
        );
    }

    public TransactionStatus validateTransactionStatus(String status) {
        try {
            return TransactionStatus.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new WrongTransactionStatusFormatException("Wrong transaction status");
        }
    }

    public TransactionStatus validateAmount(Long amount) {

        System.out.println("Amount to validate: " + amount);
        System.out.println("Limit MANUAL: " + transactionLimitService.getTransactionLimit(TransactionStatus.MANUAL_PROCESSING).getLimit());
        System.out.println("Limit ALLOWED: " + transactionLimitService.getTransactionLimit(TransactionStatus.ALLOWED).getLimit());
        if (amount == null || amount <= 0) {
            throw new InvalidAmountException("Wrong amount provided");
        } else if (amount <= transactionLimitService.getTransactionLimit(TransactionStatus.ALLOWED).getLimit()) {
            return TransactionStatus.ALLOWED;
        } else if (amount <= transactionLimitService.getTransactionLimit(TransactionStatus.MANUAL_PROCESSING).getLimit()) {
            return TransactionStatus.MANUAL_PROCESSING;
        }
        return TransactionStatus.PROHIBITED;
    }

    public TransactionStatus correlateIpAddressesOfCard(String cardNumber, String ipAddress, LocalDateTime localDateTime) {
        LocalDateTime oneHourBeforeDateTime = localDateTime.minusHours(1);
        List<String> ipList = transactionRepository
                .findAllIpAddressesByCreatedDateAfter(cardNumber, oneHourBeforeDateTime, localDateTime);
        Set<String> ipAddresses = new HashSet<>(ipList);
        ipAddresses.add(ipAddress);
        if (ipAddresses.size() == 3)
            return TransactionStatus.MANUAL_PROCESSING;
        else if (ipAddresses.size() > 3)
            return TransactionStatus.PROHIBITED;
        return TransactionStatus.ALLOWED;
    }

    public TransactionStatus correlateRegionOfCard(String cardNumber, Region region, LocalDateTime localDateTime) {
        LocalDateTime oneHourBeforeDateTime = localDateTime.minusHours(1);
        List<Region> regionList = transactionRepository
                .findAllRegionsByCardNumberAndCreatedDateAfter(cardNumber, oneHourBeforeDateTime, localDateTime);
        Set<Region> regions = new HashSet<>(regionList);
        regions.add(region);
        if (regions.size() == 3)
            return TransactionStatus.MANUAL_PROCESSING;
        else if (regions.size() > 3)
            return TransactionStatus.PROHIBITED;
        return TransactionStatus.ALLOWED;
    }

    public Region validateRegion(String region) {
        if (region == null || region.isEmpty())
            throw new InvalidRegionFormatException("Wrong region format");
        try {
            return Region.valueOf(region);
        } catch (IllegalArgumentException e) {
            throw new InvalidRegionFormatException("There is no region with name " + region);
        }
    }

    public LocalDateTime validateDate(String date) {
        if (date == null || date.isEmpty())
            throw new WrongDateTimeFormatException("Date cannot be null or empty");
        try {
            return LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new WrongDateTimeFormatException("Wrong date format");
        }
    }

    private ResponseEntity<?> transactionToResponse(Transaction transaction) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("result", transaction.getStatus(), "info", transaction.getInfo()));
    }

    private String convertlistToString(List<String> list) {
        if (list.isEmpty())
            return "none";
        return list.toString().substring(1, list.toString().length() - 1);
    }

}
