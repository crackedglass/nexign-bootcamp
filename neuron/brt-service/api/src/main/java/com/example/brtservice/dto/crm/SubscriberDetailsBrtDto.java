package com.example.brtservice.dto.crm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberDetailsBrtDto {
    private Long id;
    private String fullName;
    private String msisdn;
    private BigDecimal balance;
    private LocalDate registrationDate;
    private Long tariffId;
    // Add any other relevant fields BRT might want to return to CRM
} 