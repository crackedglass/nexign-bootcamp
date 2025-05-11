package com.example.hrsservice.impl.service;

import com.example.brtservice.api.dto.CallDataDto;
import com.example.brtservice.api.dto.RatingResponseDto;
import com.example.hrsservice.impl.entity.SubscriberMonthlyUsage;
import com.example.hrsservice.impl.entity.Tariff;
import com.example.hrsservice.impl.repository.SubscriberMonthlyUsageRepository;
import com.example.hrsservice.impl.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final TariffRepository tariffRepository;
    private final SubscriberMonthlyUsageRepository monthlyUsageRepository; // For monthly tariffs
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.hrs.name:hrs_exchange}")
    private String hrsExchangeName;

    @Value("${rabbitmq.routing.key.hrs_response.name:hrs_response_key}")
    private String hrsResponseRoutingKey;

    // Define "Romashka" MSISDN prefix (simplification)
    private static final String ROMASHKA_PREFIX = "79"; // Example prefix

    @RabbitListener(queues = "${rabbitmq.queue.hrs_rating_request.name:hrs_rating_queue}")
    @Transactional
    public void rateCall(CallDataDto callData) {
        log.info("Received call data for rating: MSISDN {}, Tariff ID {}, Call Type {}, StartTime {}",
                callData.getMsisdn(), callData.getTariffId(), callData.getCallType(), callData.getStartTime());

        Optional<Tariff> tariffOpt = tariffRepository.findById(callData.getTariffId());
        if (tariffOpt.isEmpty()) {
            log.error("Tariff with ID {} not found for MSISDN {}. Cannot rate call.", callData.getTariffId(), callData.getMsisdn());
            sendResponse(callData.getBrtCallDataId(), callData.getMsisdn(), null, false, "Tariff not found");
            return;
        }
        Tariff tariff = tariffOpt.get();
        Long cost = 0L;
        String errorMessage = null;
        boolean success = true;

        try {
            long callDurationSeconds = Duration.between(callData.getStartTime(), callData.getEndTime()).getSeconds();
            if (callDurationSeconds < 0) callDurationSeconds = 0;

            // "каждая дополнительная секунда разговора засчитывается как минута"
            long callDurationMinutes = (long) Math.ceil((double) callDurationSeconds / 60.0);
            if (callDurationSeconds > 0 && callDurationMinutes == 0) {
                 callDurationMinutes = 1; // Min 1 minute if any duration > 0
            }

            if (tariff.getTariffType() == Tariff.TariffType.CLASSIC) {
                cost = calculateClassicCost(callData, tariff, callDurationMinutes);
            } else if (tariff.getTariffType() == Tariff.TariffType.MONTHLY) {
                YearMonth usageMonth = YearMonth.from(callData.getStartTime());
                int minutesUsedThisMonth = getMinutesUsedThisMonth(callData.getBrtSubscriberId(), usageMonth);
                int includedMinutes = tariff.getIncludedMinutes() != null ? tariff.getIncludedMinutes() : 0;
                long remainingIncludedMinutes = Math.max(0, includedMinutes - minutesUsedThisMonth);

                if (callDurationMinutes <= remainingIncludedMinutes) {
                    cost = 0L; // Covered by package
                    updateMonthlyUsage(callData.getBrtSubscriberId(), usageMonth, callDurationMinutes, tariff.getId());
                } else {
                    long minutesInPackageToUse = remainingIncludedMinutes;
                    long minutesOverLimit = callDurationMinutes - minutesInPackageToUse;

                    if (minutesInPackageToUse > 0) {
                         updateMonthlyUsage(callData.getBrtSubscriberId(), usageMonth, minutesInPackageToUse, tariff.getId());
                    }

                    if (tariff.getFallbackTariffId() != null) {
                        Optional<Tariff> fallbackTariffOpt = tariffRepository.findById(tariff.getFallbackTariffId());
                        if (fallbackTariffOpt.isPresent()) {
                            cost = calculateClassicCost(callData, fallbackTariffOpt.get(), minutesOverLimit);
                        } else {
                            success = false;
                            errorMessage = "Fallback tariff ID " + tariff.getFallbackTariffId() + " not found.";
                            log.error(errorMessage);
                        }
                    } else {
                        success = false;
                        errorMessage = "Monthly tariff exceeded and no fallback tariff defined.";
                        log.error(errorMessage);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error during rating for MSISDN {}: {}", callData.getMsisdn(), e.getMessage(), e);
            success = false;
            errorMessage = "Rating calculation error: " + e.getMessage();
            cost = null; // Ensure cost is null on error if not already
        }

        sendResponse(callData.getBrtCallDataId(), callData.getMsisdn(), cost, success, errorMessage);
    }

    private Long calculateClassicCost(CallDataDto callData, Tariff classicTariff, long callDurationMinutes) {
        if (callDurationMinutes <= 0) {
            return 0L;
        }

        Long rate;
        if ("01".equals(callData.getCallType())) { // Outgoing call
            if (isRomashkaSubscriber(callData.getPeerMsisdn())) {
                rate = classicTariff.getOutgoingCallRomashkaRate();
            } else {
                rate = classicTariff.getOutgoingCallOtherRate();
            }
        } else { // Incoming call ("02")
            rate = classicTariff.getIncomingCallRate();
        }

        if (rate == null) {
            log.warn("Rate is null for tariff {} and call type {}. Assuming 0 cost.", classicTariff.getId(), callData.getCallType());
            return 0L;
        }
        return rate * callDurationMinutes;
    }

    private boolean isRomashkaSubscriber(String msisdn) {
        // Simplified check. In a real system, this might involve a lookup or a more robust check.
        return msisdn != null && msisdn.startsWith(ROMASHKA_PREFIX);
    }

    private void sendResponse(Long brtCallDataId, String msisdn, Long cost, boolean success, String errorMessage) {
        RatingResponseDto response = new RatingResponseDto(brtCallDataId, msisdn, cost, success, errorMessage);
        try {
            rabbitTemplate.convertAndSend(hrsExchangeName, hrsResponseRoutingKey, response);
            log.info("Sent rating response for MSISDN {}: Cost={}, Success={}, BRTCallDataId={}", msisdn, cost, (Object) success, brtCallDataId);
        } catch (Exception e) {
            log.error("Error sending rating response for MSISDN {}: {}", msisdn, e.getMessage(), e);
            // Consider how to handle failed response sending (e.g., retry, dead-letter)
        }
    }
    
    private Integer getMinutesUsedThisMonth(Long brtSubscriberId, YearMonth month) {
        Optional<SubscriberMonthlyUsage> usageOpt = monthlyUsageRepository.findByBrtSubscriberIdAndUsageMonth(brtSubscriberId, month.toString());
        return usageOpt.map(SubscriberMonthlyUsage::getUsedMinutes).orElse(Integer.valueOf(0));
    }

    private void updateMonthlyUsage(Long brtSubscriberId, YearMonth month, long minutesToAdd, Long tariffId) {
        if (minutesToAdd <= 0) return;

        SubscriberMonthlyUsage usage = monthlyUsageRepository.findByBrtSubscriberIdAndUsageMonth(brtSubscriberId, month.toString())
                .orElse(new SubscriberMonthlyUsage(brtSubscriberId, month, 0));

        usage.setUsedMinutes(Integer.valueOf(usage.getUsedMinutes() + (int) minutesToAdd));
        monthlyUsageRepository.save(usage);
        log.info("Updated monthly usage for subscriberId {}, month {}: added {} minutes. New total: {}. TariffId: {}", 
                 brtSubscriberId, month, (Object) minutesToAdd, (Object) usage.getUsedMinutes(), tariffId);
    }

} 