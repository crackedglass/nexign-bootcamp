package com.example.brtservice.impl.service;

import com.example.brtservice.impl.entity.Subscriber;
import com.example.brtservice.impl.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final SubscriberRepository subscriberRepository;

    /**
     * Applies the charge to the subscriber's balance.
     * @param msisdn The subscriber's phone number.
     * @param cost The cost to be deducted.
     * @param brtCallDataId The ID of the call data record in BRT, for logging/tracing.
     */
    @Transactional
    public void applyCharge(String msisdn, Long cost, Long brtCallDataId) {
        if (cost == null || cost < 0) {
            throw new IllegalArgumentException("Cost must be non-negative");
        }

        Optional<Subscriber> subscriberOpt = subscriberRepository.findByMsisdn(msisdn);
        if (subscriberOpt.isEmpty()) {
            log.error("Cannot apply charge: Subscriber with MSISDN {} not found. CallDataID: {}", msisdn, brtCallDataId);
            return;
        }

        Subscriber subscriber = subscriberOpt.get();
        Long oldBalance = subscriber.getBalance();
        Long newBalance = oldBalance - cost;
        subscriber.setBalance(newBalance);
        subscriberRepository.save(subscriber);

        log.info("Applied charge of {} to MSISDN {}. CallDataID: {}. Old balance: {}, New balance: {}",
                cost, msisdn, brtCallDataId, oldBalance, newBalance);

        // According to spec: "Если на счете абонента не хватает средств, то баланс уходит в отрицательный."
        // This is handled by simply subtracting the cost.
    }

    /**
     * Credits an amount to the subscriber's balance.
     * Used by CRM for topping up balance.
     * @param msisdn The subscriber's phone number.
     * @param amount The amount to add.
     * @return true if successful, false otherwise (e.g., subscriber not found).
     */
    @Transactional
    public boolean topUpBalance(String msisdn, Long amount) {
        if (amount == null || amount <= 0) {
            return false;
        }
        Optional<Subscriber> subscriberOpt = subscriberRepository.findByMsisdn(msisdn);
        if (subscriberOpt.isEmpty()) {
            log.warn("Cannot top up balance: Subscriber with MSISDN {} not found.", msisdn);
            return false;
        }
        Subscriber subscriber = subscriberOpt.get();
        Long oldBalance = subscriber.getBalance();
        Long newBalance = oldBalance + amount;
        subscriber.setBalance(newBalance);
        subscriberRepository.save(subscriber);
        log.info("Topped up balance for MSISDN {} by {}. Old balance: {}, New balance: {}", msisdn, amount, oldBalance, newBalance);
        return true;
    }
} 