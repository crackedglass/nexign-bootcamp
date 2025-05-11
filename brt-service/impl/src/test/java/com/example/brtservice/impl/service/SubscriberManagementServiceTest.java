package com.example.brtservice.impl.service;

import com.example.brtservice.api.dto.NewSubscriberRequestBrtDto;
import com.example.brtservice.api.dto.SubscriberDetailsBrtDto;
import com.example.brtservice.impl.entity.Subscriber;
import com.example.brtservice.impl.repository.SubscriberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SubscriberManagementServiceTest {

    @Mock
    private SubscriberRepository subscriberRepository;
    @Mock
    private BillingService billingService;

    @InjectMocks
    private SubscriberManagementService subscriberManagementService;

    private Subscriber existingSubscriber;

    @BeforeEach
    void setUp() {
        existingSubscriber = new Subscriber(1L, "79111111111", 50L, LocalDate.now().minusDays(10), 11L);
    }

    @Test
    void createSubscriber_ShouldSucceed_WhenMsisdnDoesNotExist() {
        NewSubscriberRequestBrtDto request = new NewSubscriberRequestBrtDto("79222222222", "New User", 12L, 100L);
        when(subscriberRepository.findByMsisdn("79222222222")).thenReturn(Optional.empty());
        // Mock the save operation to return a subscriber with an ID
        when(subscriberRepository.save(any(Subscriber.class)))
                .thenAnswer(invocation -> {
                    Subscriber subToSave = invocation.getArgument(0);
                    subToSave.setId(5L); // Assign a dummy ID
                    return subToSave;
                 });

        SubscriberDetailsBrtDto result = subscriberManagementService.createSubscriber(request);

        assertNotNull(result);
        assertEquals("79222222222", result.getMsisdn());
        assertEquals(100L, result.getBalance());
        assertEquals(12L, result.getTariffId());
        verify(subscriberRepository).save(any(Subscriber.class));
    }

    @Test
    void createSubscriber_ShouldThrowConflict_WhenMsisdnExists() {
        NewSubscriberRequestBrtDto request = new NewSubscriberRequestBrtDto("79111111111", "Another User", 11L, 100L);
        when(subscriberRepository.findByMsisdn("79111111111")).thenReturn(Optional.of(existingSubscriber));

        assertThrows(ResponseStatusException.class, () -> subscriberManagementService.createSubscriber(request));

        verify(subscriberRepository, never()).save(any(Subscriber.class));
    }

    @Test
    void getSubscriberByMsisdn_ShouldReturnDto_WhenFound() {
        when(subscriberRepository.findByMsisdn("79111111111")).thenReturn(Optional.of(existingSubscriber));

        SubscriberDetailsBrtDto result = subscriberManagementService.getSubscriberByMsisdn("79111111111");

        assertNotNull(result);
        assertEquals(existingSubscriber.getId(), result.getId());
        assertEquals(existingSubscriber.getMsisdn(), result.getMsisdn());
    }

    @Test
    void getSubscriberByMsisdn_ShouldThrowNotFound_WhenNotFound() {
        when(subscriberRepository.findByMsisdn("79999999999")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> subscriberManagementService.getSubscriberByMsisdn("79999999999"));
    }

    @Test
    void updateTariff_ShouldUpdateAndSave_WhenFound() {
        when(subscriberRepository.findByMsisdn("79111111111")).thenReturn(Optional.of(existingSubscriber));
        ArgumentCaptor<Subscriber> subscriberCaptor = ArgumentCaptor.forClass(Subscriber.class);
        Long newTariffId = 12L;

        subscriberManagementService.updateTariff("79111111111", newTariffId);

        verify(subscriberRepository).save(subscriberCaptor.capture());
        assertEquals(newTariffId, subscriberCaptor.getValue().getTariffId());
    }

    @Test
    void updateTariff_ShouldThrowNotFound_WhenNotFound() {
        when(subscriberRepository.findByMsisdn("79999999999")).thenReturn(Optional.empty());
        Long newTariffId = 12L;

        assertThrows(ResponseStatusException.class, () -> subscriberManagementService.updateTariff("79999999999", newTariffId));
        verify(subscriberRepository, never()).save(any(Subscriber.class));
    }

    @Test
    void topUpBalance_ShouldCallBillingService_WhenFound() {
        when(billingService.topUpBalance(eq("79111111111"), any(Long.class))).thenReturn(true);
        Long amount = 10L;

        subscriberManagementService.topUpBalance("79111111111", amount);

        verify(billingService).topUpBalance("79111111111", amount);
    }

    @Test
    void topUpBalance_ShouldThrowException_WhenBillingServiceFails() {
        when(billingService.topUpBalance(eq("79111111111"), any(Long.class))).thenReturn(false);
        // Need to mock the subsequent check for subscriber existence if billingService returns false
        when(subscriberRepository.findByMsisdn("79111111111")).thenReturn(Optional.of(existingSubscriber)); // Assume it exists but billing failed (e.g., bad amount)
        Long amount = 0L; // Example of invalid amount causing failure

        assertThrows(ResponseStatusException.class, () -> subscriberManagementService.topUpBalance("79111111111", amount));

        verify(billingService).topUpBalance("79111111111", amount);
    }
     @Test
    void topUpBalance_ShouldThrowNotFound_WhenBillingServiceFailsAndSubscriberNotFound() {
        when(billingService.topUpBalance(eq("79999999999"), any(Long.class))).thenReturn(false);
        when(subscriberRepository.findByMsisdn("79999999999")).thenReturn(Optional.empty()); // Subscriber doesn't exist
        Long amount = 10L;

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> subscriberManagementService.topUpBalance("79999999999", amount));
        
        assertEquals(404, exception.getStatusCode().value());
        verify(billingService).topUpBalance("79999999999", amount);
    }
} 