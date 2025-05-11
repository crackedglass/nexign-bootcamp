package com.example.brtservice.impl.service;

import com.example.brtservice.impl.entity.Subscriber;
import com.example.brtservice.impl.repository.SubscriberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

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
        testSubscriber = new Subscriber(1L, "79001234567", 100L, LocalDate.now(), 11L);
    }

    @Test
    void applyCharge_ShouldDecreaseBalance_WhenSubscriberExists() {
        when(subscriberRepository.findByMsisdn("79001234567")).thenReturn(Optional.of(testSubscriber));
        ArgumentCaptor<Subscriber> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);
        Long chargeAmount = 10L;

        billingService.applyCharge("79001234567", chargeAmount, 100L);

        verify(subscriberRepository).save(subscriberCaptor.capture());
        Subscriber savedSubscriber = subscriberCaptor.getValue();
        assertEquals(90L, savedSubscriber.getBalance()); // 100 - 10
    }

    @Test
    void applyCharge_ShouldHandleNegativeBalance_WhenChargeExceedsBalance() {
        when(subscriberRepository.findByMsisdn("79001234567")).thenReturn(Optional.of(testSubscriber));
        ArgumentCaptor<Subscriber> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);
        Long chargeAmount = 110L;

        billingService.applyCharge("79001234567", chargeAmount, 101L);

        verify(subscriberRepository).save(subscriberCaptor.capture());
        Subscriber savedSubscriber = subscriberCaptor.getValue();
        assertEquals(-10L, savedSubscriber.getBalance()); // 100 - 110
    }

    @Test
    void applyCharge_ShouldDoNothing_WhenSubscriberNotFound() {
        when(subscriberRepository.findByMsisdn("79999999999")).thenReturn(Optional.empty());
        Long chargeAmount = 10L;

        billingService.applyCharge("79999999999", chargeAmount, 102L);

        verify(subscriberRepository, never()).save(any(Subscriber.class));
    }

     @Test
    void applyCharge_ShouldDoNothing_WhenCostIsNegative() {
        Long chargeAmount = -10L;

        billingService.applyCharge("79001234567", chargeAmount, 103L);

        verify(subscriberRepository, never()).save(any(Subscriber.class));
        verify(subscriberRepository, never()).findByMsisdn(anyString());
    }

    @Test
    void topUpBalance_ShouldIncreaseBalance_WhenSubscriberExists() {
        when(subscriberRepository.findByMsisdn("79001234567")).thenReturn(Optional.of(testSubscriber));
        ArgumentCaptor<Subscriber> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);
        Long topUpAmount = 20L;

        boolean result = billingService.topUpBalance("79001234567", topUpAmount);

        assertTrue(result);
        verify(subscriberRepository).save(subscriberCaptor.capture());
        Subscriber savedSubscriber = subscriberCaptor.getValue();
        assertEquals(120L, savedSubscriber.getBalance()); // 100 + 20
    }

    @Test
    void topUpBalance_ShouldReturnFalse_WhenSubscriberNotFound() {
         when(subscriberRepository.findByMsisdn("79999999999")).thenReturn(Optional.empty());
         Long topUpAmount = 20L;

        boolean result = billingService.topUpBalance("79999999999", topUpAmount);

        assertFalse(result);
        verify(subscriberRepository, never()).save(any(Subscriber.class));
    }

    @Test
    void topUpBalance_ShouldReturnFalse_WhenAmountIsNotPositive() {
        Long topUpAmountZero = 0L;
        Long topUpAmountNegative = -10L;

        boolean resultZero = billingService.topUpBalance("79001234567", topUpAmountZero);
        boolean resultNegative = billingService.topUpBalance("79001234567", topUpAmountNegative);

        assertFalse(resultZero);
        assertFalse(resultNegative);
        verify(subscriberRepository, never()).save(any(Subscriber.class));
        verify(subscriberRepository, never()).findByMsisdn(anyString());
    }
} 