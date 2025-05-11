package com.example.brtservice.impl.listener;

import com.example.brtservice.api.dto.RatingResponseDto;
import com.example.brtservice.impl.service.BillingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HrsResponseMessageListenerTest {

    @Mock
    private BillingService billingService;

    // ObjectMapper mock is not needed if Spring AMQP handles conversion, 
    // and we are testing the listener method that receives the DTO directly.
    // If testing raw message handling, mock ObjectMapper.

    @InjectMocks
    private HrsResponseMessageListener hrsResponseMessageListener;

    @Test
    void receiveHrsResponse_ShouldCallApplyCharge_WhenSuccessful() {
        RatingResponseDto successfulResponse = new RatingResponseDto(
                123L, "79001112233", 5L, true, null
        );

        hrsResponseMessageListener.receiveHrsResponse(successfulResponse);

        verify(billingService, times(1)).applyCharge(
                successfulResponse.getMsisdn(),
                successfulResponse.getCost(),
                successfulResponse.getBrtCallDataId()
        );
    }

    @Test
    void receiveHrsResponse_ShouldNotCallApplyCharge_WhenUnsuccessful() {
        RatingResponseDto failedResponse = new RatingResponseDto(
                124L, "79002223344", null, false, "Tariff not found"
        );

        hrsResponseMessageListener.receiveHrsResponse(failedResponse);

        verify(billingService, never()).applyCharge(anyString(), any(Long.class), anyLong());
    }
} 