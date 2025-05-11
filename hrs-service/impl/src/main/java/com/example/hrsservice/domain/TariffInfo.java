package com.example.hrsservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TariffInfo {
    private Long id;
    private String name;
    private String tariffType;
    private BigDecimal outgoingCallRomashkaRate;
    private BigDecimal outgoingCallOtherRate;
    private BigDecimal incomingCallRate;
    private BigDecimal monthlyFee;
    private Integer includedMinutes;
    private Long fallbackTariffId;
} 