package ru.crackedglass.cdr_generator.impl.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Table(name = "calls")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_type")
    private String callType;

    @ManyToOne
    @JoinColumn(name = "subscriber_1_id")
    private SubscriberEntity subscriber1;

    @ManyToOne
    @JoinColumn(name = "subscriber_2_id")
    private SubscriberEntity subscriber2;
    
    private Instant start;

    private Instant end;
}
