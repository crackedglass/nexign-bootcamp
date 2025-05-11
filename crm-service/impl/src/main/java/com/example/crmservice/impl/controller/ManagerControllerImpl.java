package com.example.crmservice.impl.controller;

import com.example.crmservice.api.controller.ManagerController;
import com.example.crmservice.api.dto.request.ChangeTariffRequest;
import com.example.crmservice.api.dto.request.NewSubscriberRequest;
import com.example.crmservice.api.dto.request.TopUpBalanceRequest;
import com.example.crmservice.api.dto.response.SubscriberInfoResponse;
import com.example.crmservice.api.dto.response.SubscriberDetailsCrmDto;
import com.example.crmservice.impl.mapper.SubscriberMapper;
import com.example.crmservice.impl.service.ManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
public class ManagerControllerImpl implements ManagerController {

    private final ManagerService managerService;
    private final SubscriberMapper subscriberMapper;

    @Override
    public ResponseEntity<SubscriberDetailsCrmDto> createSubscriber(@Valid @RequestBody NewSubscriberRequest request) {
        var subscriber = subscriberMapper.toDomain(request);
        var createdSubscriber = managerService.createSubscriber(subscriber);
        return new ResponseEntity<>(subscriberMapper.toDto(createdSubscriber), HttpStatus.CREATED);
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