package com.example.crmservice.controller;

import com.example.crmservice.dto.request.ChangeTariffRequest;
import com.example.crmservice.dto.request.NewSubscriberRequest;
import com.example.crmservice.dto.request.TopUpBalanceRequest;
import com.example.crmservice.dto.response.SubscriberInfoResponse;
import com.example.crmservice.dto.response.SubscriberDetailsCrmDto;
import com.example.crmservice.service.ManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
public class ManagerControllerImpl implements ManagerController {

    private final ManagerService managerService;

    @Override
    public ResponseEntity<SubscriberDetailsCrmDto> createSubscriber(@Valid @RequestBody NewSubscriberRequest request) {
        SubscriberDetailsCrmDto createdSubscriber = managerService.createSubscriber(request);
        return new ResponseEntity<>(createdSubscriber, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<SubscriberInfoResponse> getSubscriberInfo(@PathVariable String msisdn) {
        SubscriberInfoResponse info = managerService.getSubscriberInfo(msisdn);
        return ResponseEntity.ok(info);
    }

    @Override
    public ResponseEntity<Void> changeTariff(@Valid @RequestBody ChangeTariffRequest request) {
        managerService.changeTariff(request.getMsisdn(), request.getNewTariffId());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> topUpBalance(@Valid @RequestBody TopUpBalanceRequest request) {
        managerService.topUpBalance(request.getMsisdn(), request.getAmount());
        return ResponseEntity.ok().build();
    }
} 