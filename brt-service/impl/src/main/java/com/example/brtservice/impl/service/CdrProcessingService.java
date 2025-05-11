package com.example.brtservice.impl.service;

import com.example.brtservice.api.dto.CallDataDto; // To be created for sending to HRS
import com.example.brtservice.impl.entity.CallData;
import com.example.brtservice.impl.entity.Subscriber;
import com.example.brtservice.impl.repository.CallDataRepository;
import com.example.brtservice.impl.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CdrProcessingService {

    private final SubscriberRepository subscriberRepository;
    private final CallDataRepository callDataRepository;
    private final RabbitTemplate rabbitTemplate; // For sending data to HRS

    @Value("${rabbitmq.exchange.hrs.name:hrs_exchange}") // Exchange for HRS
    private String hrsExchangeName;

    @Value("${rabbitmq.routing.key.hrs.name:hrs_rating_key}") // Routing key for HRS rating requests
    private String hrsRatingRoutingKey;

    private static final DateTimeFormatter CDR_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Processes the content of a CDR file string.
     * Parses each line, saves CallData, and sends relevant info to HRS for rating.
     * @param cdrFileContent The string content of the CDR file.
     */
    @Transactional
    public void processCdrFileContent(String cdrFileContent) {
        if (cdrFileContent == null || cdrFileContent.isBlank()) {
            log.warn("Received empty or null CDR file content. Skipping.");
            return;
        }

        Arrays.stream(cdrFileContent.split("\n"))
                .forEach(this::processSingleCdrRecord);
    }

    private void processSingleCdrRecord(String cdrRecordLine) {
        if (cdrRecordLine.isBlank()) return;

        String[] parts = cdrRecordLine.split(",");
        if (parts.length != 5) {
            log.error("Invalid CDR record format: {}. Expected 5 parts.", cdrRecordLine);
            return;
        }

        try {
            String callType = parts[0].trim();
            String msisdn = parts[1].trim();
            String peerMsisdn = parts[2].trim();
            LocalDateTime startTime = LocalDateTime.parse(parts[3].trim(), CDR_DATE_TIME_FORMATTER);
            LocalDateTime endTime = LocalDateTime.parse(parts[4].trim(), CDR_DATE_TIME_FORMATTER);

            Optional<Subscriber> subscriberOpt = subscriberRepository.findByMsisdn(msisdn);
            if (subscriberOpt.isEmpty()) {
                log.warn("Subscriber with MSISDN {} not found in BRT. Skipping CDR record: {}", msisdn, cdrRecordLine);
                return;
            }
            Subscriber subscriber = subscriberOpt.get();

            CallData callData = new CallData();
            callData.setSubscriber(subscriber);
            callData.setCallType(callType);
            callData.setPeerMsisdn(peerMsisdn);
            callData.setStartTime(startTime);
            callData.setEndTime(endTime);
            CallData savedCallData = callDataRepository.save(callData);
            log.info("Saved CallData with ID: {} for MSISDN: {}", savedCallData.getId(), msisdn);

            // Prepare and send data to HRS for rating
            sendToHrsForRating(savedCallData, subscriber.getTariffId());

        } catch (DateTimeParseException e) {
            log.error("Error parsing date-time in CDR record: {}. Details: {}", cdrRecordLine, e.getMessage());
        } catch (Exception e) {
            log.error("Error processing CDR record: {}. Details: {}", cdrRecordLine, e.getMessage(), e);
        }
    }

    private void sendToHrsForRating(CallData callData, Long tariffId) {
        CallDataDto callDataDto = new CallDataDto(
                callData.getId(), // BRT CallData ID for correlation
                callData.getSubscriber().getMsisdn(),
                callData.getCallType(),
                callData.getPeerMsisdn(),
                callData.getStartTime(),
                callData.getEndTime(),
                tariffId,
                // We might need to send subscriber's current state for monthly tariffs (e.g., used minutes)
                // For now, keeping it simple as per current understanding.
                callData.getSubscriber().getId() // Sending subscriberId from BRT
        );

        try {
            rabbitTemplate.convertAndSend(hrsExchangeName, hrsRatingRoutingKey, callDataDto);
            log.info("Sent call data (ID: {}) for MSISDN {} to HRS for rating.", callData.getId(), callData.getSubscriber().getMsisdn());
        } catch (Exception e) {
            log.error("Error sending call data (ID: {}) to HRS for MSISDN {}: {}",
                    callData.getId(), callData.getSubscriber().getMsisdn(), e.getMessage(), e);
            // Consider retry or dead-letter queue for failed messages
        }
    }
} 