package com.example.brtservice.service;

import com.example.brtservice.dto.crm.NewSubscriberRequestBrtDto;
import com.example.brtservice.dto.crm.SubscriberDetailsBrtDto;
import com.example.brtservice.entity.Subscriber;
import com.example.brtservice.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriberManagementService {

    private final SubscriberRepository subscriberRepository;
    private final BillingService billingService; // Reusing existing service for top-up

    @Transactional
    public SubscriberDetailsBrtDto createSubscriber(NewSubscriberRequestBrtDto request) {
        log.info("Attempting to create subscriber with MSISDN: {}", request.getMsisdn());
        if (subscriberRepository.findByMsisdn(request.getMsisdn()).isPresent()) {
            log.warn("Subscriber creation failed: MSISDN {} already exists.", request.getMsisdn());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Subscriber with this MSISDN already exists.");
        }
        // TODO: Consider validating tariffId exists (e.g., via call to HRS, or assume valid for now)

        Subscriber newSubscriber = new Subscriber();
        newSubscriber.setMsisdn(request.getMsisdn());
        newSubscriber.setFullName(request.getFullName());
        newSubscriber.setTariffId(request.getTariffId());
        newSubscriber.setBalance(request.getInitialBalance()); // Use provided balance (default 100)
        newSubscriber.setRegistrationDate(LocalDate.now());

        Subscriber saved = subscriberRepository.save(newSubscriber);
        log.info("Successfully created subscriber ID {} with MSISDN {}", saved.getId(), saved.getMsisdn());
        return mapToDetailsDto(saved);
    }

    public SubscriberDetailsBrtDto getSubscriberByMsisdn(String msisdn) {
        log.debug("Fetching subscriber by MSISDN: {}", msisdn);
        Subscriber subscriber = subscriberRepository.findByMsisdn(msisdn)
                .orElseThrow(() -> {
                    log.warn("Subscriber not found by MSISDN: {}", msisdn);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscriber not found");
                });
        return mapToDetailsDto(subscriber);
    }

    @Transactional
    public void updateTariff(String msisdn, Long newTariffId) {
        log.info("Attempting to update tariff for MSISDN {} to {}", msisdn, newTariffId);
        // TODO: Consider validating newTariffId exists (e.g., via call to HRS)
        Subscriber subscriber = subscriberRepository.findByMsisdn(msisdn)
                .orElseThrow(() -> {
                    log.warn("Update tariff failed: Subscriber not found by MSISDN: {}", msisdn);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscriber not found");
                });
        subscriber.setTariffId(newTariffId);
        subscriberRepository.save(subscriber);
        log.info("Successfully updated tariff for MSISDN {} to {}", msisdn, newTariffId);
    }

    @Transactional
    public void topUpBalance(String msisdn, BigDecimal amount) {
        log.info("Attempting to top up balance for MSISDN {} by {}", msisdn, amount);
        boolean success = billingService.topUpBalance(msisdn, amount);
        if (!success) {
            // BillingService logs specific reasons, check if subscriber exists
             if (subscriberRepository.findByMsisdn(msisdn).isEmpty()) {
                 throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscriber not found");
             } else {
                 // Likely non-positive amount, which billingService handles
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid top-up amount.");
             }
        }
        log.info("Successfully processed top-up request for MSISDN {}", msisdn);
    }

    private SubscriberDetailsBrtDto mapToDetailsDto(Subscriber subscriber) {
        return new SubscriberDetailsBrtDto(
                subscriber.getId(),
                subscriber.getFullName(),
                subscriber.getMsisdn(),
                subscriber.getBalance(),
                subscriber.getRegistrationDate(),
                subscriber.getTariffId()
        );
    }
} 