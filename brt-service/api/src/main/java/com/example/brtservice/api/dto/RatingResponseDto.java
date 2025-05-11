package com.example.brtservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponseDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long brtCallDataId; // Correlates with the original CallData in BRT
    private String msisdn;        // Subscriber MSISDN
    private Long cost;      // Calculated cost of the call
    private boolean success;      // Was rating successful?
    private String errorMessage;  // Error message if not successful
} 