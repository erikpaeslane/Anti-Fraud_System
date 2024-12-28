package antifraud.controller;

import antifraud.service.StolenCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class StolenCardController {

    private final StolenCardService stolenCardService;

    @Autowired
    public StolenCardController(StolenCardService stolenCardService) {
        this.stolenCardService = stolenCardService;
    }

    @PostMapping("/api/antifraud/stolencard")
    public ResponseEntity<?> addStolenCard(@RequestBody StolenCardCredentials stolenCardCredentials) {
        return stolenCardService.addStolenCard(stolenCardCredentials.number());
    }

    @GetMapping("api/antifraud/stolencard")
    public ResponseEntity<?> getAllStolenCards() {
        return stolenCardService.getaAllStolenCards();
    }

    @DeleteMapping("api/antifraud/stolencard/{number}")
    public ResponseEntity<?> deleteStolenCard(@PathVariable String number) {
        return stolenCardService.removeStolenCard(number);
    }

    public record StolenCardCredentials(String number) {}
}
