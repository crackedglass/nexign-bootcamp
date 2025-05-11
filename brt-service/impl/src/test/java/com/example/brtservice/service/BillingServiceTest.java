package com.example.brtservice.service;

import com.example.brtservice.entity.Subscriber;
import com.example.brtservice.repository.SubscriberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private SubscriberRepository subscriberRepository;

    @InjectMocks
    private BillingService billingService;

    private Subscriber testSubscriber;

    @BeforeEach
    void setUp() {
        testSubscriber = new Subscriber(1L, "Test User", "79001234567", BigDecimal.valueOf(100.50), LocalDate.now(), 11L);
    }

    @Test
    void applyCharge_ShouldDecreaseBalance_WhenSubscriberExists() {
        when(subscriberRepository.findByMsisdn("79001234567")).thenReturn(Optional.of(testSubscriber));
        ArgumentCaptor<Subscriber> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);
        BigDecimal chargeAmount = BigDecimal.valueOf(10.25);

        billingService.applyCharge("79001234567", chargeAmount, 100L);

        verify(subscriberRepository).save(subscriberCaptor.capture());
        Subscriber savedSubscriber = subscriberCaptor.getValue();
        assertEquals(BigDecimal.valueOf(90.25), savedSubscriber.getBalance()); // 100.50 - 10.25
    }

    @Test
    void applyCharge_ShouldHandleNegativeBalance_WhenChargeExceedsBalance() {
        when(subscriberRepository.findByMsisdn("79001234567")).thenReturn(Optional.of(testSubscriber));
        ArgumentCaptor<Subscriber> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);
        BigDecimal chargeAmount = BigDecimal.valueOf(110.00);

        billingService.applyCharge("79001234567", chargeAmount, 101L);

        verify(subscriberRepository).save(subscriberCaptor.capture());
        Subscriber savedSubscriber = subscriberCaptor.getValue();
        assertEquals(BigDecimal.valueOf(-9.50), savedSubscriber.getBalance()); // 100.50 - 110.00
    }

    @Test
    void applyCharge_ShouldDoNothing_WhenSubscriberNotFound() {
        when(subscriberRepository.findByMsisdn("79999999999")).thenReturn(Optional.empty());
        BigDecimal chargeAmount = BigDecimal.valueOf(10.00);

        billingService.applyCharge("79999999999", chargeAmount, 102L);

        verify(subscriberRepository, never()).save(any(Subscriber.class));
    }

     @Test
    void applyCharge_ShouldDoNothing_WhenCostIsNegative() {
        BigDecimal chargeAmount = BigDecimal.valueOf(-10.00);

        billingService.applyCharge("79001234567", chargeAmount, 103L);

        verify(subscriberRepository, never()).save(any(Subscriber.class));
        verify(subscriberRepository, never()).findByMsisdn(anyString());
    }

    @Test
    void topUpBalance_ShouldIncreaseBalance_WhenSubscriberExists() {
        when(subscriberRepository.findByMsisdn("79001234567")).thenReturn(Optional.of(testSubscriber));
        ArgumentCaptor<Subscriber> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);
        BigDecimal topUpAmount = BigDecimal.valueOf(20.00);

        boolean result = billingService.topUpBalance("79001234567", topUpAmount);

        assertTrue(result);
        verify(subscriberRepository).save(subscriberCaptor.capture());
        Subscriber savedSubscriber = subscriberCaptor.getValue();
        assertEquals(BigDecimal.valueOf(120.50), savedSubscriber.getBalance()); // 100.50 + 20.00
    }

    @Test
    void topUpBalance_ShouldReturnFalse_WhenSubscriberNotFound() {
         when(subscriberRepository.findByMsisdn("79999999999")).thenReturn(Optional.empty());
         BigDecimal topUpAmount = BigDecimal.valueOf(20.00);

        boolean result = billingService.topUpBalance("79999999999", topUpAmount);

        assertFalse(result);
        verify(subscriberRepository, never()).save(any(Subscriber.class));
    }

    @Test
    void topUpBalance_ShouldReturnFalse_WhenAmountIsNotPositive() {
        BigDecimal topUpAmountZero = BigDecimal.ZERO;
        BigDecimal topUpAmountNegative = BigDecimal.valueOf(-10.00);

        boolean resultZero = billingService.topUpBalance("79001234567", topUpAmountZero);
        boolean resultNegative = billingService.topUpBalance("79001234567", topUpAmountNegative);

        assertFalse(resultZero);
        assertFalse(resultNegative);
        verify(subscriberRepository, never()).save(any(Subscriber.class));
        verify(subscriberRepository, never()).findByMsisdn(anyString());
    }
} 