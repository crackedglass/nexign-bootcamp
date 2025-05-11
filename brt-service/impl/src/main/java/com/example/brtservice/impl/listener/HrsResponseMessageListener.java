package com.example.brtservice.impl.listener;

import com.example.brtservice.api.dto.RatingResponseDto;
import com.example.brtservice.impl.service.BillingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HrsResponseMessageListener {

    private final BillingService billingService;
    private final ObjectMapper objectMapper; // Spring Boot auto-configures this

    @RabbitListener(queues = "${rabbitmq.queue.hrs_response.name:hrs_response_queue}")
    public void receiveHrsResponse(RatingResponseDto ratingResponse) {
        log.info("Received HRS response for MSISDN {}: Cost = {}, Success = {}",
                ratingResponse.getMsisdn(), ratingResponse.getCost(), ratingResponse.isSuccess());

        if (ratingResponse.isSuccess()) {
            billingService.applyCharge(ratingResponse.getMsisdn(), ratingResponse.getCost(), ratingResponse.getBrtCallDataId());
        } else {
            log.error("HRS rating failed for MSISDN {}. CallDataID: {}. Error: {}",
                    ratingResponse.getMsisdn(), ratingResponse.getBrtCallDataId(), ratingResponse.getErrorMessage());
            // Potentially: mark the call record as failed to rate, notify admin, etc.
        }
    }
} 