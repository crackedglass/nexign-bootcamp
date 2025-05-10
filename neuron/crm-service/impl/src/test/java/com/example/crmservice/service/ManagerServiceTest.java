package com.example.crmservice.service;

import com.example.brtservice.client.BrtServiceClient;
import com.example.brtservice.client.dto.NewSubscriberRequestBrt;
import com.example.crmservice.dto.request.NewSubscriberRequest;
import com.example.crmservice.dto.response.SubscriberDetailsCrmDto;
import com.example.crmservice.dto.response.SubscriberInfoResponse;
import com.example.crmservice.dto.response.TariffInfoCrmDto;
import com.example.hrsservice.feign.HrsServiceClient;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private BrtServiceClient brtServiceClient;
    @Mock
    private HrsServiceClient hrsServiceClient;

    @InjectMocks
    private ManagerService managerService;

    private SubscriberDetailsCrmDto mockBrtSubscriberDetails;
    private TariffInfoCrmDto mockHrsTariffInfo;

    @BeforeEach
    void setUp() {
        mockBrtSubscriberDetails = new SubscriberDetailsCrmDto(1L, "Test User", "79001234567", BigDecimal.TEN, LocalDate.now(), 11L);
        mockHrsTariffInfo = new TariffInfoCrmDto(11L, "Классика", "CLASSIC", BigDecimal.valueOf(1.5), BigDecimal.valueOf(2.5), BigDecimal.ZERO, null, null, null);
    }

    // Helper to create FeignException
    private FeignException createFeignException(int status, String content) {
        return FeignException.errorStatus("methodKey",
                feign.Response.builder()
                        .status(status)
                        .reason("Some Reason")
                        .request(Request.create(Request.HttpMethod.GET, "/url", Collections.emptyMap(), null, new RequestTemplate()))
                        .body(content, StandardCharsets.UTF_8)
                        .build());
    }

    @Test
    void createSubscriber_ShouldSucceed_WhenDependenciesSucceed() {
        NewSubscriberRequest request = new NewSubscriberRequest("79001234567", "Test User", 11L, BigDecimal.valueOf(100));
        when(hrsServiceClient.getTariffById(11L)).thenReturn(ResponseEntity.ok(mockHrsTariffInfo));
        when(brtServiceClient.createSubscriber(any(com.example.crmservice.client.dto.NewSubscriberRequestBrt.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(mockBrtSubscriberDetails));

        SubscriberDetailsCrmDto result = managerService.createSubscriber(request);

        assertNotNull(result);
        assertEquals("79001234567", result.getMsisdn());
        verify(hrsServiceClient).getTariffById(11L);
        verify(brtServiceClient).createSubscriber(any(com.example.crmservice.client.dto.NewSubscriberRequestBrt.class));
    }

    @Test
    void createSubscriber_ShouldThrowBadRequest_WhenHrsValidationFailsNotFound() {
        NewSubscriberRequest request = new NewSubscriberRequest("79001234567", "Test User", 99L, BigDecimal.valueOf(100));
        when(hrsServiceClient.getTariffById(99L)).thenThrow(createFeignException(404, "Tariff 99 not found"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            managerService.createSubscriber(request);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Tariff ID 99 does not exist"));
        verify(brtServiceClient, never()).createSubscriber(any());
    }

    @Test
    void createSubscriber_ShouldThrowConflict_WhenBrtReturnsConflict() {
        NewSubscriberRequest request = new NewSubscriberRequest("79001234567", "Test User", 11L, BigDecimal.valueOf(100));
        when(hrsServiceClient.getTariffById(11L)).thenReturn(ResponseEntity.ok(mockHrsTariffInfo));
        when(brtServiceClient.createSubscriber(any(NewSubscriberRequestBrt.class)))
                .thenThrow(createFeignException(409, "Subscriber already exists"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            managerService.createSubscriber(request);
        });
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Subscriber already exists"));
    }

    @Test
    void getSubscriberInfo_ShouldReturnCombinedInfo_WhenDependenciesSucceed() {
        when(brtServiceClient.getSubscriberByMsisdn("79001234567")).thenReturn(ResponseEntity.ok(mockBrtSubscriberDetails));
        when(hrsServiceClient.getTariffById(11L)).thenReturn(ResponseEntity.ok(mockHrsTariffInfo));

        SubscriberInfoResponse result = managerService.getSubscriberInfo("79001234567");

        assertNotNull(result);
        assertEquals(mockBrtSubscriberDetails, result.getSubscriberDetails());
        assertEquals(mockHrsTariffInfo, result.getTariffInfo());
        verify(brtServiceClient).getSubscriberByMsisdn("79001234567");
        verify(hrsServiceClient).getTariffById(11L);
    }

    @Test
    void getSubscriberInfo_ShouldReturnPartialInfo_WhenHrsFails() {
         when(brtServiceClient.getSubscriberByMsisdn("79001234567")).thenReturn(ResponseEntity.ok(mockBrtSubscriberDetails));
         when(hrsServiceClient.getTariffById(11L)).thenThrow(createFeignException(500, "HRS Internal Error"));

         SubscriberInfoResponse result = managerService.getSubscriberInfo("79001234567");

         assertNotNull(result);
         assertEquals(mockBrtSubscriberDetails, result.getSubscriberDetails());
         assertNull(result.getTariffInfo()); // Tariff info should be null
         verify(brtServiceClient).getSubscriberByMsisdn("79001234567");
         verify(hrsServiceClient).getTariffById(11L);
    }

    @Test
    void getSubscriberInfo_ShouldThrowNotFound_WhenBrtReturnsNotFound() {
        when(brtServiceClient.getSubscriberByMsisdn("79001234567")).thenThrow(createFeignException(404, "Not Found"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
             managerService.getSubscriberInfo("79001234567");
         });
         assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
         verify(hrsServiceClient, never()).getTariffById(anyLong());
    }

    @Test
    void changeTariff_ShouldSucceed_WhenDependenciesSucceed() {
        when(hrsServiceClient.getTariffById(12L)).thenReturn(ResponseEntity.ok(mockHrsTariffInfo)); // Assume tariff 12 exists
        when(brtServiceClient.updateSubscriberTariff("79001234567", 12L)).thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> {
            managerService.changeTariff("79001234567", 12L);
        });

        verify(hrsServiceClient).getTariffById(12L);
        verify(brtServiceClient).updateSubscriberTariff("79001234567", 12L);
    }

    @Test
    void topUpBalance_ShouldSucceed_WhenBrtSucceeds() {
        BigDecimal amount = BigDecimal.valueOf(50);
        when(brtServiceClient.topUpBalance("79001234567", amount)).thenReturn(ResponseEntity.ok().build());

         assertDoesNotThrow(() -> {
             managerService.topUpBalance("79001234567", amount);
         });

         verify(brtServiceClient).topUpBalance("79001234567", amount);
    }

     @Test
    void topUpBalance_ShouldThrowBadRequest_WhenAmountNotPositive() {
        BigDecimal amount = BigDecimal.ZERO;
       
         ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
             managerService.topUpBalance("79001234567", amount);
         });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
         verify(brtServiceClient, never()).topUpBalance(anyString(), any());
    }
} 