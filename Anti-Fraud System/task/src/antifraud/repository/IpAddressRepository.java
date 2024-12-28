package antifraud.repository;

import antifraud.entity.IpAddress;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IpAddressRepository extends CrudRepository<IpAddress, Long> {
    Optional<IpAddress> findIpAddressByIp(String ip);
    List<IpAddress> findAllByOrderByIdAsc();
    boolean existsIpAddressByIp(String ip);
}
