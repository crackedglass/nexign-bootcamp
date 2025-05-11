package com.example.crmservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewSubscriberRequest {
    @NotBlank(message = "MSISDN cannot be blank")
    @Pattern(regexp = "^7[0-9]{10}$", message = "MSISDN must be 11 digits starting with 7 (e.g., 79001234567)")
    private String msisdn;

    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @NotNull(message = "Tariff ID cannot be null")
    private Long tariffId;

    // Spec: "баланс которого по умолчанию 100 у.е."
    // This field is for potential override, but service will default to 100.
    @PositiveOrZero(message = "Initial balance must be positive or zero")
    private BigDecimal initialBalance = BigDecimal.valueOf(100.00); // Default here, can be set by manager
} 