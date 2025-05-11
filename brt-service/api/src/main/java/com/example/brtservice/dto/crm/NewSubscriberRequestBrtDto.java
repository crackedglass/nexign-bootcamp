package com.example.brtservice.dto.crm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewSubscriberRequestBrtDto {
    @NotBlank
    private String msisdn;
    @NotBlank
    private String fullName;
    @NotNull
    private Long tariffId;
    @NotNull
    private BigDecimal initialBalance;
} 