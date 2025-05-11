package com.example.crmservice.impl.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.brtservice.api.feign.BrtServiceClient;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriberService {

    private final BrtServiceClient brtServiceClient;

    public void topUpOwnBalance(String msisdn, Long amount) {
        log.info("Subscriber {} topping up own balance by amount {}", msisdn, amount);
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        try {
            ResponseEntity<Void> response = brtServiceClient.topUpBalance(msisdn, amount);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Error topping up balance for subscriber {} in BRT. Status: {}", msisdn, response.getStatusCode());
                throw new ResponseStatusException(response.getStatusCode(), "BRT service error during balance top-up.");
            }
            log.info("Balance for subscriber {} topped up by {} successfully via BRT.", msisdn, amount);
        } catch (FeignException e) {
            log.error("FeignException while topping up balance for subscriber {} in BRT, Content: {}", msisdn,  e.contentUTF8());
            if (e.status() == HttpStatus.NOT_FOUND.value()) {
                // This could happen if the subscriber was deleted from BRT after authentication token was issued
                // Or if in-memory user list in CRM is out of sync with BRT.
                log.warn("Authenticated subscriber {} not found in BRT during top-up.", msisdn);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Your subscriber account was not found. Please contact support.");
            }
            throw new ResponseStatusException(HttpStatus.valueOf(e.status()), "Could not top up balance due to an issue with the billing service: " + e.contentUTF8());
        } catch (Exception e) {
            log.error("Unexpected error topping up balance for subscriber {}: {}", msisdn, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while topping up your balance.");
        }
    }
} 