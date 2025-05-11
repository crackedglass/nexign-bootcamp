package com.example.brtservice.impl.controller;

import com.example.brtservice.api.controller.BrtInternalController;
import com.example.brtservice.api.dto.NewSubscriberRequestBrtDto; // DTO for BRT to receive
import com.example.brtservice.api.dto.SubscriberDetailsBrtDto; // DTO for BRT to return
import com.example.brtservice.impl.service.SubscriberManagementService; // New service in BRT
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class BrtInternalControllerImpl implements BrtInternalController {

    private final SubscriberManagementService subscriberManagementService;

    @Override
    public ResponseEntity<SubscriberDetailsBrtDto> createSubscriber(@Valid @RequestBody NewSubscriberRequestBrtDto request) {
        SubscriberDetailsBrtDto createdSubscriber = subscriberManagementService.createSubscriber(request);
        return new ResponseEntity<>(createdSubscriber, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<SubscriberDetailsBrtDto> getSubscriberByMsisdn(@PathVariable String msisdn) {
        SubscriberDetailsBrtDto subscriber = subscriberManagementService.getSubscriberByMsisdn(msisdn);
        return ResponseEntity.ok(subscriber);
    }

    @Override 
    public ResponseEntity<Void> updateSubscriberTariff(@PathVariable String msisdn, @RequestParam Long newTariffId) {
        subscriberManagementService.updateTariff(msisdn, newTariffId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> topUpBalance(@PathVariable String msisdn, @RequestParam Long amount) {
        subscriberManagementService.topUpBalance(msisdn, amount);
        return ResponseEntity.ok().build();
    }
} 