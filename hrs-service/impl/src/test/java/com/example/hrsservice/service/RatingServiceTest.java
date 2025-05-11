// package com.example.hrsservice.service;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.timeout;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.time.YearMonth;
// import java.util.Optional;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.amqp.rabbit.core.RabbitTemplate;
// import org.springframework.test.util.ReflectionTestUtils;

// import com.example.hrsservice.dto.CallDataDto;
// import com.example.hrsservice.dto.RatingResponseDto;
// import com.example.hrsservice.entity.SubscriberMonthlyUsage;
// import com.example.hrsservice.entity.Tariff;
// import com.example.hrsservice.repository.SubscriberMonthlyUsageRepository;
// import com.example.hrsservice.repository.TariffRepository;

// @ExtendWith(MockitoExtension.class)
// class RatingServiceTest {

//     @Mock
//     private TariffRepository tariffRepository;
//     @Mock
//     private SubscriberMonthlyUsageRepository monthlyUsageRepository;
//     @Mock
//     private RabbitTemplate rabbitTemplate;

//     @InjectMocks
//     private RatingService ratingService;

//     private final String testHrsExchange = "hrs_exchange";
//     private final String testResponseRoutingKey = "hrs_response_key";

//     private Tariff classicTariff;
//     private Tariff monthlyTariff;
//     private Tariff classicFallbackTariff; // Used by monthlyTariff

//     @BeforeEach
//     void setUp() {
//         ReflectionTestUtils.setField(ratingService, "hrsExchangeName", testHrsExchange);
//         ReflectionTestUtils.setField(ratingService, "responseRoutingKey", testResponseRoutingKey);

//         classicTariff = new Tariff(11L, "Классика", Tariff.TariffType.CLASSIC,
//                 BigDecimal.valueOf(1.50), BigDecimal.valueOf(2.50), BigDecimal.ZERO,
//                 null, null, null);

//         classicFallbackTariff = classicTariff; // For simplicity, monthly falls back to the main classic tariff

//         monthlyTariff = new Tariff(12L, "Помесячный", Tariff.TariffType.MONTHLY,
//                 null, null, null, // Rates are null, uses fallback
//                 BigDecimal.valueOf(100.00), 50, classicFallbackTariff.getId());
//     }

//     private CallDataDto createCallDataDto(Long tariffId, String msisdn, String callType, String peerMsisdn, LocalDateTime start, LocalDateTime end, Long brtSubId, Long brtCallId) {
//         return new CallDataDto(brtCallId, msisdn, callType, peerMsisdn, start, end, tariffId, brtSubId);
//     }

//     private ArgumentCaptor<RatingResponseDto> verifyAndCaptureResponse() {
//         ArgumentCaptor<RatingResponseDto> responseCaptor = ArgumentCaptor.forClass(RatingResponseDto.class);
//         verify(rabbitTemplate, timeout(100).times(1)) // Use timeout for async verification if needed, though listener is sync here
//                 .convertAndSend(eq(testHrsExchange), eq(testResponseRoutingKey), responseCaptor.capture());
//         return responseCaptor;
//     }

//     @Test
//     void rateCall_Classic_Incoming_ShouldBeZeroCost() {
//         when(tariffRepository.findById(11L)).thenReturn(Optional.of(classicTariff));
//         CallDataDto callData = createCallDataDto(11L, "79001", "02", "79002",
//                 LocalDateTime.parse("2025-03-10T10:00:00"), LocalDateTime.parse("2025-03-10T10:10:00"), 1L, 101L);

//         ratingService.rateCall(callData);

//         RatingResponseDto response = verifyAndCaptureResponse().getValue();
//         assertTrue(response.isSuccess());
//         assertEquals(BigDecimal.ZERO.setScale(2), response.getCost());
//         assertEquals(101L, response.getBrtCallDataId());
//     }

//     @Test
//     void rateCall_Classic_OutgoingRomashka_ShouldCalculateCost() {
//         when(tariffRepository.findById(11L)).thenReturn(Optional.of(classicTariff));
//         // Peer starts with 79 (Romashka)
//         CallDataDto callData = createCallDataDto(11L, "79001", "01", "79002",
//                 LocalDateTime.parse("2025-03-10T11:00:00"), LocalDateTime.parse("2025-03-10T11:01:11"), 1L, 102L);

//         ratingService.rateCall(callData);

//         RatingResponseDto response = verifyAndCaptureResponse().getValue();
//         assertTrue(response.isSuccess());
//         // Duration 1:11 -> 2 minutes. Rate 1.50. Cost = 3.00
//         assertEquals(BigDecimal.valueOf(3.00).setScale(2), response.getCost());
//     }

//     @Test
//     void rateCall_Classic_OutgoingOther_ShouldCalculateCost() {
//         when(tariffRepository.findById(11L)).thenReturn(Optional.of(classicTariff));
//         // Peer does not start with 79
//         CallDataDto callData = createCallDataDto(11L, "79001", "01", "78123456789",
//                 LocalDateTime.parse("2025-03-10T12:00:00"), LocalDateTime.parse("2025-03-10T12:00:30"), 1L, 103L);

//         ratingService.rateCall(callData);

//         RatingResponseDto response = verifyAndCaptureResponse().getValue();
//         assertTrue(response.isSuccess());
//         // Duration 0:30 -> 1 minute. Rate 2.50. Cost = 2.50
//         assertEquals(BigDecimal.valueOf(2.50).setScale(2), response.getCost());
//     }

//     @Test
//     void rateCall_Monthly_WithinLimit_ShouldBeZeroCost_AndUsageUpdated() {
//         when(tariffRepository.findById(12L)).thenReturn(Optional.of(monthlyTariff));
//         // Assume 20 minutes used so far this month
//         YearMonth month = YearMonth.parse("2025-03");
//         when(monthlyUsageRepository.findByBrtSubscriberIdAndUsageMonth(eq(2L), eq("2025-03")))
//                 .thenReturn(Optional.of(new SubscriberMonthlyUsage(10L, 2L, "2025-03", 20)));
        
//         CallDataDto callData = createCallDataDto(12L, "79003", "01", "79004",
//                 LocalDateTime.parse("2025-03-10T14:00:00"), LocalDateTime.parse("2025-03-10T14:15:00"), 2L, 104L);
//         // Call duration 15 minutes. Available: 50 - 20 = 30. Call fits.

//         ratingService.rateCall(callData);

//         RatingResponseDto response = verifyAndCaptureResponse().getValue();
//         assertTrue(response.isSuccess());
//         assertEquals(BigDecimal.ZERO.setScale(2), response.getCost());

//         // Verify usage was updated
//         ArgumentCaptor<SubscriberMonthlyUsage> usageCaptor = ArgumentCaptor.forClass(SubscriberMonthlyUsage.class);
//         verify(monthlyUsageRepository).save(usageCaptor.capture());
//         assertEquals(2L, usageCaptor.getValue().getBrtSubscriberId());
//         assertEquals("2025-03", usageCaptor.getValue().getUsageMonth());
//         assertEquals(20 + 15, usageCaptor.getValue().getUsedMinutes()); // 20 previous + 15 current
//     }
    
//     @Test
//     void rateCall_Monthly_ExceedsLimit_ShouldUseFallbackTariff_AndUsageUpdated() {
//         when(tariffRepository.findById(12L)).thenReturn(Optional.of(monthlyTariff));
//         when(tariffRepository.findById(11L)).thenReturn(Optional.of(classicFallbackTariff)); // Mock fallback tariff fetch
//          // Assume 40 minutes used so far this month
//         YearMonth month = YearMonth.parse("2025-03");
//         SubscriberMonthlyUsage initialUsage = new SubscriberMonthlyUsage(11L, 2L, "2025-03", 40);
//         when(monthlyUsageRepository.findByBrtSubscriberIdAndUsageMonth(eq(2L), eq("2025-03")))
//                 .thenReturn(Optional.of(initialUsage));
                
//         CallDataDto callData = createCallDataDto(12L, "79003", "01", "79004", // Outgoing Romashka
//                 LocalDateTime.parse("2025-03-15T10:00:00"), LocalDateTime.parse("2025-03-15T10:25:00"), 2L, 105L);
//         // Call duration 25 minutes. Available: 50 - 40 = 10 minutes.
//         // 10 minutes covered. 15 minutes over limit.
        
//         ratingService.rateCall(callData);

//         RatingResponseDto response = verifyAndCaptureResponse().getValue();
//         assertTrue(response.isSuccess());
//         // Cost for 15 mins over limit using fallback (Классика, Romashka rate 1.50) = 15 * 1.50 = 22.50
//         assertEquals(BigDecimal.valueOf(22.50).setScale(2), response.getCost());

//         // Verify usage was updated to use the remaining 10 mins
//         ArgumentCaptor<SubscriberMonthlyUsage> usageCaptor = ArgumentCaptor.forClass(SubscriberMonthlyUsage.class);
//         verify(monthlyUsageRepository).save(usageCaptor.capture());
//         assertEquals(40 + 10, usageCaptor.getValue().getUsedMinutes()); // Should be capped at included minutes (50)
//     }

//     @Test
//     void rateCall_ShouldSendFailureResponse_WhenTariffNotFound() {
//          when(tariffRepository.findById(99L)).thenReturn(Optional.empty());
//          CallDataDto callData = createCallDataDto(99L, "79001", "01", "79002",
//                 LocalDateTime.parse("2025-03-10T10:00:00"), LocalDateTime.parse("2025-03-10T10:10:00"), 1L, 106L);

//         ratingService.rateCall(callData);

//         RatingResponseDto response = verifyAndCaptureResponse().getValue();
//         assertFalse(response.isSuccess());
//         assertNull(response.getCost());
//         assertEquals("Tariff not found", response.getErrorMessage());
//         assertEquals(106L, response.getBrtCallDataId());
//     }
// } 