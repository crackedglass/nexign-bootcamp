package com.example.crmservice.impl.domain;

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
    private Long initialBalance = 100L;
} 