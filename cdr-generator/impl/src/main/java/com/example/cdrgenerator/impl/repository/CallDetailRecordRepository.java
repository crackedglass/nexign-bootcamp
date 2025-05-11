package com.example.cdrgenerator.impl.repository;

import com.example.cdrgenerator.impl.entity.CallDetailRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallDetailRecordRepository extends JpaRepository<CallDetailRecord, Long> {

    @Query("SELECT c FROM CallDetailRecord c WHERE c.processed = false ORDER BY c.startTime")
    List<CallDetailRecord> findUnprocessedRecords();

    @Query("SELECT c FROM CallDetailRecord c WHERE c.processed = false ORDER BY c.startTime")
    List<CallDetailRecord> findUnprocessedBatch();
} 