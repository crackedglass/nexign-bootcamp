package com.example.brtservice.impl.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
    private Long brtCallDataId;
    private String msisdn;
    private BigDecimal cost;
    private boolean success;
    private String errorMessage;
} 