package ru.crackedglass.cdr_generator.impl.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Table(name = "calls")
@Entity
public class Call {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "subscriber_id")
    Subscriber caller;

    @ManyToOne
    @JoinColumn(name = "subscriber_id")
    Subscriber receiver;
    
    Instant start;

    Instant end;
}
