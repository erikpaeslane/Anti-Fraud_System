package antifraud.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ip_addresses")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class IpAddress {

    @Id
    @GeneratedValue
    @Column(name="id")
    private long id;
    @Column(name="ip")
    private String ip;

    public IpAddress(String ip) {
        this.ip = ip;
    }
}
