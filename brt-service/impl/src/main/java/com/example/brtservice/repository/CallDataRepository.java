package com.example.brtservice.repository;

import com.example.brtservice.entity.CallData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CallDataRepository extends JpaRepository<CallData, Long> {
    // Custom query methods can be added here if needed
} 