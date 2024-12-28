package antifraud.entity;

import antifraud.model.Region;
import antifraud.model.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="transactions")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Builder
public class Transaction {

    @Id
    @GeneratedValue
    @Column
    @JsonProperty("transactionId")
    private int id;

    @Column(name = "amount")
    private long amount;

    @Column(name = "ip_address")
    @JsonProperty("ip")
    private String ipAddress;

    @Column(name = "card_number")
    @JsonProperty("number")
    private String cardNumber;

    @Column(name = "region")
    @Enumerated(EnumType.STRING)
    private Region region;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "info")
    private String info;

    @Builder.Default
    @Column(name="feedback")
    @Enumerated(EnumType.STRING)
    private TransactionStatus feedback = null;

}
