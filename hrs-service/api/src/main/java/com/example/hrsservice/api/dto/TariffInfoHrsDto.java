package com.example.hrsservice.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TariffInfoHrsDto {
    private Long id;
    private String name;
    private String tariffType;
    private Long outgoingCallRomashkaRate;
    private Long outgoingCallOtherRate;
    private Long incomingCallRate;
    private Long monthlyFee;
    private Integer includedMinutes;
    private Long fallbackTariffId;
    private Long minuteRate;
    private Integer freeMinutes;
} 