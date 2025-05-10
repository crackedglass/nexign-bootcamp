package com.example.crmservice.dto.response;

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
    private BigDecimal outgoingCallRomashkaRate;
    private BigDecimal outgoingCallOtherRate;
    private BigDecimal incomingCallRate;
    private BigDecimal monthlyFee;
    private Integer includedMinutes;
    private Long fallbackTariffId; // ID of tariff for over-limit usage
} 