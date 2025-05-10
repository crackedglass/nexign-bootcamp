package com.example.brtservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "subscribers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(unique = true, nullable = false)
    private String msisdn; // Phone number

    @Column(precision = 10, scale = 2)
    private BigDecimal balance;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "tariff_id", nullable = false)
    private Long tariffId; // References a tariff in HRS (e.g., 11 for Classic, 12 for Monthly)
} 