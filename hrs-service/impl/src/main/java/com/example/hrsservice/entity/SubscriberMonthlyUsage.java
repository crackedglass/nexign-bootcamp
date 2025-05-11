package com.example.hrsservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Entity
@Table(name = "subscriber_monthly_usage",
       uniqueConstraints = @UniqueConstraint(columnNames = {"brt_subscriber_id", "usage_month"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberMonthlyUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brt_subscriber_id", nullable = false)
    private Long brtSubscriberId; // ID of the subscriber in BRT service

    @Column(name = "usage_month", nullable = false, columnDefinition = "VARCHAR(7)") // Store as YYYY-MM string
    private String usageMonth; // Format: YYYY-MM, e.g., "2025-02"

    @Column(name = "used_minutes", nullable = false)
    private int usedMinutes;

    public SubscriberMonthlyUsage(Long brtSubscriberId, YearMonth usageMonth, int usedMinutes) {
        this.brtSubscriberId = brtSubscriberId;
        this.usageMonth = usageMonth.toString(); // Store as YYYY-MM
        this.usedMinutes = usedMinutes;
    }

    public YearMonth getUsageMonthAsYearMonth() {
        return YearMonth.parse(this.usageMonth);
    }

    public void setUsageMonth(YearMonth usageMonth) {
        this.usageMonth = usageMonth.toString();
    }
} 