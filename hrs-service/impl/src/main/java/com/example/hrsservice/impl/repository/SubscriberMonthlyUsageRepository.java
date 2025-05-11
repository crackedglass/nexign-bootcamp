package com.example.hrsservice.impl.repository;

import com.example.hrsservice.impl.entity.SubscriberMonthlyUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriberMonthlyUsageRepository extends JpaRepository<SubscriberMonthlyUsage, Long> {

    // Find usage by BRT subscriber ID and usage month (stored as YYYY-MM string)
    Optional<SubscriberMonthlyUsage> findByBrtSubscriberIdAndUsageMonth(Long brtSubscriberId, String usageMonth);

} 