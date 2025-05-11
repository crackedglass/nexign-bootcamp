package com.example.brtservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallDataDto implements Serializable { // Implement Serializable for AMQP
    private static final long serialVersionUID = 1L;

    private Long brtCallDataId; // ID of the CallData record in BRT for correlation
    private String msisdn; // Subscriber's phone number
    private String callType; // 01 or 02
    private String peerMsisdn;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long tariffId; // Subscriber's current tariff ID
    private Long brtSubscriberId; // Subscriber ID in BRT
    // Add other fields if HRS needs more info, e.g., for monthly tariffs, current month's usage.
} 