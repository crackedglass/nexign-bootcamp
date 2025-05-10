package com.example.brtservice.controller;

import com.example.brtservice.dto.crm.NewSubscriberRequestBrtDto; // DTO for BRT to receive
import com.example.brtservice.dto.crm.SubscriberDetailsBrtDto; // DTO for BRT to return
import com.example.brtservice.service.SubscriberManagementService; // New service in BRT
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/internal/subscribers") // For inter-service communication
@RequiredArgsConstructor
public class BrtInternalController {

    private final SubscriberManagementService subscriberManagementService;

    @PostMapping
    public ResponseEntity<SubscriberDetailsBrtDto> createSubscriber(@Valid @RequestBody NewSubscriberRequestBrtDto request) {
        SubscriberDetailsBrtDto createdSubscriber = subscriberManagementService.createSubscriber(request);
        return new ResponseEntity<>(createdSubscriber, HttpStatus.CREATED);
    }

    @GetMapping("/msisdn/{msisdn}")
    public ResponseEntity<SubscriberDetailsBrtDto> getSubscriberByMsisdn(@PathVariable String msisdn) {
        SubscriberDetailsBrtDto subscriber = subscriberManagementService.getSubscriberByMsisdn(msisdn);
        return ResponseEntity.ok(subscriber);
    }

    @PutMapping("/{msisdn}/tariff")
    public ResponseEntity<Void> updateSubscriberTariff(@PathVariable String msisdn, @RequestParam Long newTariffId) {
        subscriberManagementService.updateTariff(msisdn, newTariffId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{msisdn}/balance/top-up")
    public ResponseEntity<Void> topUpBalance(@PathVariable String msisdn, @RequestParam BigDecimal amount) {
        subscriberManagementService.topUpBalance(msisdn, amount);
        return ResponseEntity.ok().build();
    }
} 