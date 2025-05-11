package com.example.crmservice.impl.controller;

import com.example.crmservice.api.dto.request.TopUpBalanceRequest;
import com.example.crmservice.impl.service.SubscriberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriber")
@RequiredArgsConstructor
public class SubscriberController {

    private final SubscriberService subscriberService;

    @PostMapping("/payment")
    public ResponseEntity<Void> topUpOwnBalance(@Valid @RequestBody TopUpBalanceRequest request,
                                                Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String msisdn = userDetails.getUsername(); // MSISDN is the username for subscribers
        
        // The request.msisdn can be ignored or validated against principal's msisdn
        if (request.getMsisdn() != null && !request.getMsisdn().equals(msisdn)){
            // This case should ideally not happen if clients are well-behaved
            // or DTO for subscriber might not even have msisdn field.
             return ResponseEntity.badRequest().build(); // Or throw an exception
        }

        subscriberService.topUpOwnBalance(msisdn, request.getAmount());
        return ResponseEntity.ok().build();
    }
} 