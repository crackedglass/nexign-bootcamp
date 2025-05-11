package com.example.crmservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewSubscriber {
    private String msisdn;
    private String fullName;
    private Long tariffId;
    private BigDecimal initialBalance = BigDecimal.valueOf(100.00);
} 