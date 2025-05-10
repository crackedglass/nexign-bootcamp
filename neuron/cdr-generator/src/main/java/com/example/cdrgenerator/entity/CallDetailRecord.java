package com.example.cdrgenerator.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "call_detail_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallDetailRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String callType; // 01 for outgoing, 02 for incoming
    private String msisdn; // Serviced subscriber number
    private String peerMsisdn; // The other party's number
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Flag to indicate if this record has been processed and sent in a CDR file
    private boolean processed = false;

    public String toCdrString() {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return String.join(",",
                callType,
                msisdn,
                peerMsisdn,
                startTime.format(formatter),
                endTime.format(formatter)
        );
    }
} 