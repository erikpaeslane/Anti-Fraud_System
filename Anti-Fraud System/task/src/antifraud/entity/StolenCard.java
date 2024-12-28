package antifraud.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stolen_cards")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class StolenCard {

    @Id
    @GeneratedValue
    @Column(name="id")
    private long id;
    @Column(name="card_number")
    private String number;

    public StolenCard(String number) {
        this.number = number;
    }
}
