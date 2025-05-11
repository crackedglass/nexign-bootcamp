package com.example.brtservice.impl.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@Table(name = "subscribers")
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String msisdn;

    @Column(nullable = false)
    private Long tariffId;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Long balance;

    public Subscriber(Long id, String msisdn, Long balance, LocalDate registrationDate, Long tariffId) {
        this.id = id;
        this.msisdn = msisdn;
        this.balance = balance;
        this.tariffId = tariffId;
    }
} 