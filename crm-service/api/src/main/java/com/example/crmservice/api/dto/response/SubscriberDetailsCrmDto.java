package com.example.crmservice.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberDetailsCrmDto {
    private Long id;
    private String fullName;
    private String msisdn;
    private Long balance;
    private LocalDate registrationDate;
    private Long tariffId; // Just the ID, full tariff info will be nested or separate
} 