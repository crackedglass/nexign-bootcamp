package com.example.crmservice.controller;

import com.example.crmservice.dto.request.ChangeTariffRequest;
import com.example.crmservice.dto.request.NewSubscriberRequest;
import com.example.crmservice.dto.request.TopUpBalanceRequest;
import com.example.crmservice.dto.response.SubscriberDetailsCrmDto;
import com.example.crmservice.dto.response.SubscriberInfoResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager")
public interface ManagerController {
    
    @PostMapping("/subscribers")
    public ResponseEntity<SubscriberDetailsCrmDto> createSubscriber(@Valid @RequestBody NewSubscriberRequest request);

    @GetMapping("/subscribers/{msisdn}/info")
    public ResponseEntity<SubscriberInfoResponse> getSubscriberInfo(@PathVariable String msisdn);

    @PutMapping("/subscribers/tariff")
    public ResponseEntity<Void> changeTariff(@Valid @RequestBody ChangeTariffRequest request);

    @PostMapping("/subscribers/balance/top-up")
    public ResponseEntity<Void> topUpBalance(@Valid @RequestBody TopUpBalanceRequest request);
}