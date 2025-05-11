package com.example.hrsservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tariffs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tariff {

    @Id
    private Long id; // e.g., 11, 12

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Классика", "Помесячный"

    @Enumerated(EnumType.STRING)
    @Column(name = "tariff_type", nullable = false)
    private TariffType tariffType; // CLASSIC, MONTHLY

    // Fields for "Классика" type and fallback for "Помесячный"
    @Column(name = "outgoing_call_romashka_rate", precision = 10, scale = 2) // Cost per minute
    private BigDecimal outgoingCallRomashkaRate;

    @Column(name = "outgoing_call_other_rate", precision = 10, scale = 2) // Cost per minute
    private BigDecimal outgoingCallOtherRate;

    @Column(name = "incoming_call_rate", precision = 10, scale = 2) // Cost per minute (usually 0)
    private BigDecimal incomingCallRate;

    // Fields for "Помесячный" type
    @Column(name = "monthly_fee", precision = 10, scale = 2)
    private BigDecimal monthlyFee;

    @Column(name = "included_minutes")
    private Integer includedMinutes; // Combined for incoming/outgoing

    // Tariff ID to use for calls exceeding included minutes in a monthly plan
    @Column(name = "fallback_tariff_id")
    private Long fallbackTariffId; // e.g., ID of a "Классика" tariff

    public enum TariffType {
        CLASSIC, // Тариф "Классика"
        MONTHLY  // Тариф "Помесячный"
    }
} 