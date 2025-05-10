package com.example.brtservice.service;

import com.example.brtservice.dto.CallDataDto;
import com.example.brtservice.entity.CallData;
import com.example.brtservice.entity.Subscriber;
import com.example.brtservice.repository.CallDataRepository;
import com.example.brtservice.repository.SubscriberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CdrProcessingServiceTest {

    @Mock
    private SubscriberRepository subscriberRepository;
    @Mock
    private CallDataRepository callDataRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CdrProcessingService cdrProcessingService;

    private final String testHrsExchange = "hrs_exchange";
    private final String testHrsRoutingKey = "hrs_rating_key";

    private Subscriber testSubscriber;
    private CallData savedCallData;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cdrProcessingService, "hrsExchangeName", testHrsExchange);
        ReflectionTestUtils.setField(cdrProcessingService, "hrsRatingRoutingKey", testHrsRoutingKey);

        testSubscriber = new Subscriber(1L, "Test User", "79001234567", BigDecimal.valueOf(100.0), LocalDate.now(), 11L);
        savedCallData = new CallData(100L, testSubscriber, "01", "79009876543", LocalDateTime.now().minusMinutes(5), LocalDateTime.now());

        // Common mocking setup
        when(subscriberRepository.findByMsisdn(anyString())).thenReturn(Optional.of(testSubscriber));
        when(callDataRepository.save(any(CallData.class))).thenReturn(savedCallData);
    }

    @Test
    void processCdrFileContent_ShouldProcessValidRecords() {
        String cdrLine1 = "01,79001234567,79009876543,2025-01-01T10:00:00,2025-01-01T10:05:00";
        String cdrLine2 = "02,79001234567,79001112233,2025-01-01T11:00:00,2025-01-01T11:15:00";
        String cdrFileContent = cdrLine1 + "\n" + cdrLine2;

        cdrProcessingService.processCdrFileContent(cdrFileContent);

        // Verify save called twice
        verify(callDataRepository, times(2)).save(any(CallData.class));
        // Verify message sent to HRS twice
        verify(rabbitTemplate, times(2)).convertAndSend(eq(testHrsExchange), eq(testHrsRoutingKey), any(CallDataDto.class));
    }

    @Test
    void processCdrFileContent_ShouldSkipRecord_WhenSubscriberNotFound() {
        String cdrLine = "01,79999999999,79009876543,2025-01-01T10:00:00,2025-01-01T10:05:00";
        when(subscriberRepository.findByMsisdn("79999999999")).thenReturn(Optional.empty());

        cdrProcessingService.processCdrFileContent(cdrLine);

        verify(callDataRepository, never()).save(any(CallData.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any());
    }

    @Test
    void processCdrFileContent_ShouldSkipRecord_WhenInvalidFormat() {
        String cdrLine = "01,79001234567,invalid-record"; // Invalid format

        cdrProcessingService.processCdrFileContent(cdrLine);

        verify(callDataRepository, never()).save(any(CallData.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any());
    }

    @Test
    void processCdrFileContent_ShouldSkipRecord_WhenInvalidDate() {
        String cdrLine = "01,79001234567,79009876543,invalid-date,2025-01-01T10:05:00";

        cdrProcessingService.processCdrFileContent(cdrLine);

        verify(callDataRepository, never()).save(any(CallData.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any());
    }

    @Test
    void sendToHrsForRating_ShouldSendCorrectDto() {
        ArgumentCaptor<CallDataDto> dtoCaptor = ArgumentCaptor.forClass(CallDataDto.class);

        // Trigger the private method through the public one
        String cdrLine = "01,79001234567,79009876543,2025-01-01T10:00:00,2025-01-01T10:05:00";
        cdrProcessingService.processCdrFileContent(cdrLine);

        verify(rabbitTemplate).convertAndSend(eq(testHrsExchange), eq(testHrsRoutingKey), dtoCaptor.capture());
        
        CallDataDto sentDto = dtoCaptor.getValue();
        assertEquals(savedCallData.getId(), sentDto.getBrtCallDataId());
        assertEquals(testSubscriber.getMsisdn(), sentDto.getMsisdn());
        assertEquals(testSubscriber.getId(), sentDto.getBrtSubscriberId());
        assertEquals(testSubscriber.getTariffId(), sentDto.getTariffId());
        assertEquals("01", sentDto.getCallType());
        assertEquals(LocalDateTime.parse("2025-01-01T10:00:00"), sentDto.getStartTime());
         assertEquals(LocalDateTime.parse("2025-01-01T10:05:00"), sentDto.getEndTime());
    }
} 