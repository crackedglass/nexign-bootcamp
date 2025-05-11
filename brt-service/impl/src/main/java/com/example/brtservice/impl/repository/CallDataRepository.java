package com.example.brtservice.impl.repository;

import com.example.brtservice.impl.entity.CallData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CallDataRepository extends JpaRepository<CallData, Long> {
}