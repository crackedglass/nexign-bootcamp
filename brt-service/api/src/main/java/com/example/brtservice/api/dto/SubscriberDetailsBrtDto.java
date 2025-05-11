package com.example.brtservice.api.dto;

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
    private String msisdn;
    private Long balance;
    private Long tariffId;
}