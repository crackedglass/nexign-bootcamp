package com.example.crmservice.service;

import com.example.crmservice.client.BrtServiceClient;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SubscriberServiceTest {

    @Mock
    private BrtServiceClient brtServiceClient;

    @InjectMocks
    private SubscriberService subscriberService;

    // Helper to create FeignException
    private FeignException createFeignException(int status, String content) {
        return FeignException.errorStatus("methodKey",
                feign.Response.builder()
                        .status(status)
                        .reason("Some Reason")
                        .request(Request.create(Request.HttpMethod.POST, "/url", Collections.emptyMap(), null, new RequestTemplate()))
                        .body(content, StandardCharsets.UTF_8)
                        .build());
    }

    @Test
    void topUpOwnBalance_ShouldSucceed_WhenBrtSucceeds() {
        String msisdn = "79001112233";
        BigDecimal amount = BigDecimal.TEN;
        when(brtServiceClient.topUpBalance(msisdn, amount)).thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> {
            subscriberService.topUpOwnBalance(msisdn, amount);
        });

        verify(brtServiceClient).topUpBalance(msisdn, amount);
    }

    @Test
    void topUpOwnBalance_ShouldThrowBadRequest_WhenAmountNotPositive() {
        String msisdn = "79001112233";
        BigDecimal amount = BigDecimal.ZERO;

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            subscriberService.topUpOwnBalance(msisdn, amount);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Top-up amount must be positive"));
        verify(brtServiceClient, never()).topUpBalance(anyString(), any());
    }

    @Test
    void topUpOwnBalance_ShouldThrowNotFound_WhenBrtReturnsNotFound() {
        String msisdn = "79001112233";
        BigDecimal amount = BigDecimal.TEN;
        when(brtServiceClient.topUpBalance(msisdn, amount)).thenThrow(createFeignException(404, "Subscriber not found"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            subscriberService.topUpOwnBalance(msisdn, amount);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Your subscriber account was not found"));
    }

    @Test
    void topUpOwnBalance_ShouldThrowServiceUnavailable_WhenBrtReturnsError() {
        String msisdn = "79001112233";
        BigDecimal amount = BigDecimal.TEN;
        when(brtServiceClient.topUpBalance(msisdn, amount)).thenThrow(createFeignException(500, "BRT Internal Error"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            subscriberService.topUpOwnBalance(msisdn, amount);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode()); // Or whatever status the FeignException had
        assertTrue(exception.getReason().contains("Could not top up balance due to an issue with the billing service"));
    }
} 