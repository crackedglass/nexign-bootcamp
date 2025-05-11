package com.example.crmservice.impl.domain;

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
    private Long balance;
    private LocalDate registrationDate;
    private Long tariffId;
} 