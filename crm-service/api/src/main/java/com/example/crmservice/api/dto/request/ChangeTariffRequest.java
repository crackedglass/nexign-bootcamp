package com.example.crmservice.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeTariffRequest {
    @NotBlank(message = "MSISDN cannot be blank")
    @Pattern(regexp = "^7[0-9]{10}$", message = "MSISDN must be 11 digits starting with 7")
    private String msisdn;

    @NotNull(message = "New Tariff ID cannot be null")
    private Long newTariffId;
} 