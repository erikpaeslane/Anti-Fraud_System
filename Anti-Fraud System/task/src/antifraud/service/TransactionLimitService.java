package antifraud.service;

import antifraud.entity.TransactionLimit;
import antifraud.model.TransactionStatus;
import antifraud.repository.TransactionLimitRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TransactionLimitService {

    private final TransactionLimitRepository transactionLimitRepository;

    @Autowired
    public TransactionLimitService(TransactionLimitRepository transactionLimitRepository) {
        this.transactionLimitRepository = transactionLimitRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeTransactionLimits() {
        if (transactionLimitRepository.count() == 0) {
            List<TransactionLimit> initialLimits = Arrays.asList(
                    new TransactionLimit(TransactionStatus.ALLOWED, 200),
                    new TransactionLimit(TransactionStatus.MANUAL_PROCESSING, 1500)
            );
            transactionLimitRepository.saveAll(initialLimits);
        }
    }

    public List<TransactionLimit> getTransactionLimits() {
        return transactionLimitRepository.findAll();
    }

    public TransactionLimit getTransactionLimit(TransactionStatus status) {
        return transactionLimitRepository.findByStatus(status).orElse(null);
    }

    public void increaseLimit(TransactionStatus status, long amount) {
        TransactionLimit transactionLimit = transactionLimitRepository.findByStatus(status).orElse(null);
        if (transactionLimit == null) {
            return;
        }
        int newLimit = calculateNewLimit("increase", transactionLimit.getLimit(), (int) amount);
        transactionLimit.setLimit(newLimit);
        transactionLimitRepository.save(transactionLimit);
    }

    public void decreaseLimit(TransactionStatus status, long amount) {
        TransactionLimit transactionLimit = transactionLimitRepository.findByStatus(status).orElse(null);
        if (transactionLimit == null) {
            return;
        }
        int newLimit = calculateNewLimit("decrease", transactionLimit.getLimit(), (int) amount);
        transactionLimit.setLimit(newLimit);
        transactionLimitRepository.save(transactionLimit);

    }

    private int calculateNewLimit(String operation, int limit, int amount) {
        if (operation.equals("increase"))
            return (int) (Math.ceil((0.8 * limit + 0.2 * amount)));
        else if (operation.equals("decrease"))
            return (int) (Math.ceil((0.8 * limit - 0.2 * amount)));
        else
            throw new IllegalArgumentException("Invalid operation: " + operation);
    }
}
