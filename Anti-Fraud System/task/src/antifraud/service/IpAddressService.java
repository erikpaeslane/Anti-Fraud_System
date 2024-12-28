package antifraud.service;

import antifraud.entity.IpAddress;
import antifraud.repository.IpAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class IpAddressService {

    private final IpAddressRepository ipAddressRepository;

    private final static String CORRECT_IP_ADDRESS_FORMAT =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    @Autowired
    public IpAddressService(IpAddressRepository ipAddressRepository) {
        this.ipAddressRepository = ipAddressRepository;
    }

    public ResponseEntity<?> addSuspiciousId(String ip) {
        if (isIpAddressNotValid(ip)){
            return ResponseEntity.badRequest().build();
        }
        if (existsSuspiciousIpAddress(ip))
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        IpAddress ipAddress = new IpAddress(ip);
        ipAddressRepository.save(ipAddress);
        return ResponseEntity.status(HttpStatus.OK).body(ipAddress);
    }

    public ResponseEntity<?> removeSuspiciousId(String ip) {
        if (isIpAddressNotValid(ip))
            return ResponseEntity.badRequest().build();

        return ipAddressRepository.findIpAddressByIp(ip)
                .map(existing -> {
                    ipAddressRepository.delete(existing);
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(Map.of("status", ("IP " + ip + " successfully removed!")));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    public ResponseEntity<?> getAllSuspiciousIpAddresses() {
        return ResponseEntity.status(HttpStatus.OK).body(ipAddressRepository.findAllByOrderByIdAsc());
    }

    public boolean existsSuspiciousIpAddress(String ip) {
        return ipAddressRepository.existsIpAddressByIp(ip);
    }

    private boolean isIpAddressNotValid(String ip) {
        return !ip.matches(CORRECT_IP_ADDRESS_FORMAT);
    }

}
