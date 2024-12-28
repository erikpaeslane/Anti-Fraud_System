package antifraud.entity;

import antifraud.model.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="transaction_limits")
@NoArgsConstructor
@AllArgsConstructor
@Setter @Getter
public class TransactionLimit {

    @Id
    @GeneratedValue
    private int id;
    @Enumerated(EnumType.STRING)
    @Column(name="transaction_status")
    private TransactionStatus status;
    @Column(name = "transaction_limit")
    private int limit;

    public TransactionLimit(TransactionStatus status, int limit) {
        this.status = status;
        this.limit = limit;
    }
}
