package antifraud.controller;

import antifraud.service.IpAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class IpAddressController {

    private final IpAddressService ipAddressService;

    @Autowired
    public IpAddressController(IpAddressService ipAddressService) {
        this.ipAddressService = ipAddressService;
    }

    @PostMapping("/api/antifraud/suspicious-ip")
    public ResponseEntity<?> addSuspiciousIp(@RequestBody IpAddressCredentials ipAddressCredentials) {
        return ipAddressService.addSuspiciousId(ipAddressCredentials.ip());
    }

    @GetMapping("/api/antifraud/suspicious-ip")
    public ResponseEntity<?> getAllSuspiciousIpAddresses() {
        return ipAddressService.getAllSuspiciousIpAddresses();
    }

    @DeleteMapping("/api/antifraud/suspicious-ip/{ip}")
    public ResponseEntity<?> deleteSuspiciousIpAddress(@PathVariable String ip) {
        return ipAddressService.removeSuspiciousId(ip);
    }

    public record IpAddressCredentials(String ip) {}
}
