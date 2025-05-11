package com.example.crmservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberDetails {
    private Long id;
    private String fullName;
    private String msisdn;
    private BigDecimal balance;
    private LocalDate registrationDate;
    private Long tariffId;
} 