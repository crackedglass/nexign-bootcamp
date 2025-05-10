package com.example.cdrgenerator.repository;

import com.example.cdrgenerator.entity.CallDetailRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallDetailRecordRepository extends JpaRepository<CallDetailRecord, Long> {

    // Find unprocessed records, ordered by start time (to maintain chronological order in CDRs)
    @Query("SELECT cdr FROM CallDetailRecord cdr WHERE cdr.processed = false ORDER BY cdr.startTime ASC")
    List<CallDetailRecord> findUnprocessedRecords();

    // Find a batch of unprocessed records
    // Note: For simplicity, fetching all. In a high-volume system, use pagination.
    List<CallDetailRecord> findByProcessedFalseOrderByStartTimeAsc();
} 