package com.example.brtservice.impl.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallData {
    private Long brtCallDataId;
    private String msisdn;
    private String callType;
    private String peerMsisdn;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long tariffId;
    private Long brtSubscriberId;
} 