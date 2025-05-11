package com.example.cdrgenerator.impl.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public Subscriber(String msisdn) {
        this.msisdn = msisdn;
    }
} 