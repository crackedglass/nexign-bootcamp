package com.example.crmservice.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopUpBalanceRequest {
    // MSISDN is optional for subscriber (taken from principal), required for manager
    @Pattern(regexp = "^7[0-9]{10}$", message = "MSISDN must be 11 digits starting with 7, if provided for manager.")
    private String msisdn; // Can be null if subscriber is topping up their own balance

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.1", inclusive = true, message = "Amount must be at least 0.1")
    private Long amount;
} 