package com.example.crmservice.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TariffInfoCrmDto {
    private Long id;
    private String name;
    private String tariffType; // e.g., CLASSIC, MONTHLY

    // Relevant pricing details, can be expanded
    private Long outgoingCallRomashkaRate;
    private Long outgoingCallOtherRate;
    private Long incomingCallRate;
    private Long monthlyFee;
    private Integer includedMinutes;
    private Long fallbackTariffId; // ID of tariff for over-limit usage
} 