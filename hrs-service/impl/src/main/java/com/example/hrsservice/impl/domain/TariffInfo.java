package com.example.hrsservice.impl.domain;

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
    private Long outgoingCallRomashkaRate;
    private Long outgoingCallOtherRate;
    private Long incomingCallRate;
    private Long monthlyFee;
    private Integer includedMinutes;
    private Long fallbackTariffId;
} 