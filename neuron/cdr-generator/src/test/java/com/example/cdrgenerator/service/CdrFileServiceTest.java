package com.example.cdrgenerator.service;

import com.example.cdrgenerator.entity.CallDetailRecord;
import com.example.cdrgenerator.repository.CallDetailRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CdrFileServiceTest {

    @Mock
    private CallDetailRecordRepository callDetailRecordRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CdrFileService cdrFileService;

    private final String testExchange = "test_exchange";
    private final String testRoutingKey = "test_key";

    @BeforeEach
    void setUp() {
        // Inject mock values for exchange and routing key using ReflectionTestUtils
        ReflectionTestUtils.setField(cdrFileService, "exchangeName", testExchange);
        ReflectionTestUtils.setField(cdrFileService, "routingKey", testRoutingKey);
    }

    private List<CallDetailRecord> createMockRecords(int count) {
        LocalDateTime time = LocalDateTime.of(2025, 1, 1, 10, 0);
        return IntStream.range(0, count)
                .mapToObj(i -> new CallDetailRecord((long)i, "01", "7900000000" + i % 10,
                        "7911111111" + i % 10, time.plusMinutes(i), time.plusMinutes(i + 5), false))
                .collect(Collectors.toList());
    }

    @Test
    void processAndSendCdrFiles_ShouldDoNothing_WhenNoUnprocessedRecords() {
        when(callDetailRecordRepository.findByProcessedFalseOrderByStartTimeAsc()).thenReturn(List.of());

        cdrFileService.processAndSendCdrFiles();

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
        verify(callDetailRecordRepository, never()).save(any(CallDetailRecord.class));
    }

    @Test
    void processAndSendCdrFiles_ShouldSendOneFullBatch_When10RecordsExist() {
        List<CallDetailRecord> records = createMockRecords(10);
        when(callDetailRecordRepository.findByProcessedFalseOrderByStartTimeAsc()).thenReturn(records);
        ArgumentCaptor<CallDetailRecord> savedRecordCaptor = ArgumentCaptor.forClass(CallDetailRecord.class);

        cdrFileService.processAndSendCdrFiles();

        // Verify one message sent
        verify(rabbitTemplate, times(1)).convertAndSend(eq(testExchange), eq(testRoutingKey), anyString());
        // Verify all 10 records saved (marked as processed)
        verify(callDetailRecordRepository, times(10)).save(savedRecordCaptor.capture());
        assertTrue(savedRecordCaptor.getAllValues().stream().allMatch(CallDetailRecord::isProcessed));
    }

    @Test
    void processAndSendCdrFiles_ShouldSendTwoFullBatches_When25RecordsExist() {
        List<CallDetailRecord> records = createMockRecords(25);
        when(callDetailRecordRepository.findByProcessedFalseOrderByStartTimeAsc()).thenReturn(records);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<CallDetailRecord> savedRecordCaptor = ArgumentCaptor.forClass(CallDetailRecord.class);

        cdrFileService.processAndSendCdrFiles();

        // Verify two messages sent (2 full batches of 10)
        verify(rabbitTemplate, times(2)).convertAndSend(eq(testExchange), eq(testRoutingKey), messageCaptor.capture());
        // Verify first 20 records saved (marked as processed)
        verify(callDetailRecordRepository, times(20)).save(savedRecordCaptor.capture());
        assertTrue(savedRecordCaptor.getAllValues().stream().allMatch(CallDetailRecord::isProcessed));

        // Check content (optional, but good)
        assertEquals(10, messageCaptor.getAllValues().get(0).split("\n").length);
        assertEquals(10, messageCaptor.getAllValues().get(1).split("\n").length);
    }

    @Test
    void processAndSendCdrFiles_ShouldNotSendPartialBatch_WhenMoreRecordsExist() {
        // Simulate 15 records available now, but more might come later
        List<CallDetailRecord> records = createMockRecords(15);
        // Simulate that there are more records potentially available beyond the initial fetch (size > batch size)
         when(callDetailRecordRepository.findByProcessedFalseOrderByStartTimeAsc()).thenReturn(records);
         // The logic checks unprocessedRecords.size() > i + CDR_BATCH_SIZE, so 15 > 0 + 10 is true

        cdrFileService.processAndSendCdrFiles();

        // Should send only the first full batch of 10
        verify(rabbitTemplate, times(1)).convertAndSend(eq(testExchange), eq(testRoutingKey), anyString());
        verify(callDetailRecordRepository, times(10)).save(any(CallDetailRecord.class));
    }
    
    @Test
    void processAndSendCdrFiles_ShouldSendPartialBatch_WhenItsTheLastBatch() {
         // Simulate exactly 7 records remaining, no more potentially available later
        List<CallDetailRecord> records = createMockRecords(7); 
        when(callDetailRecordRepository.findByProcessedFalseOrderByStartTimeAsc()).thenReturn(records);
        // The check unprocessedRecords.size() > i + CDR_BATCH_SIZE fails (7 > 0 + 10 is false), so partial batch is sent.
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        cdrFileService.processAndSendCdrFiles();

        // Should send the partial batch of 7
        verify(rabbitTemplate, times(1)).convertAndSend(eq(testExchange), eq(testRoutingKey), messageCaptor.capture());
        verify(callDetailRecordRepository, times(7)).save(any(CallDetailRecord.class));
        assertEquals(7, messageCaptor.getValue().split("\n").length); // Check content size
    }

    @Test
    void processAndSendCdrFiles_ShouldHandleRabbitMqErrorGracefully() {
        List<CallDetailRecord> records = createMockRecords(12);
        when(callDetailRecordRepository.findByProcessedFalseOrderByStartTimeAsc()).thenReturn(records);
        // Simulate error when sending the first batch
        doThrow(new RuntimeException("RabbitMQ connection failed"))
                .when(rabbitTemplate).convertAndSend(eq(testExchange), eq(testRoutingKey), anyString());

        cdrFileService.processAndSendCdrFiles();

        // Verify template was called once (and failed)
        verify(rabbitTemplate, times(1)).convertAndSend(eq(testExchange), eq(testRoutingKey), anyString());
        // Verify NO records were marked as processed because the transaction should roll back (or save wasn't reached)
        verify(callDetailRecordRepository, never()).save(any(CallDetailRecord.class));
    }
} 