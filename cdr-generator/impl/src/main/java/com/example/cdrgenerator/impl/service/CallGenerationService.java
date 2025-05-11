package com.example.cdrgenerator.impl.service;

import com.example.cdrgenerator.impl.entity.CallDetailRecord;
import com.example.cdrgenerator.impl.entity.Subscriber;
import com.example.cdrgenerator.impl.repository.CallDetailRecordRepository;
import com.example.cdrgenerator.impl.repository.SubscriberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CallGenerationService {

    private final SubscriberRepository subscriberRepository;
    private final CallDetailRecordRepository callDetailRecordRepository;
    private final Random random = new Random();
    private List<Subscriber> subscribers;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // For parallel generation

    // Define the simulation period (1 year)
    private static final LocalDateTime SIMULATION_START_DATE = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
    private static final LocalDateTime SIMULATION_END_DATE = LocalDateTime.of(2025, 12, 31, 23, 59, 59);

    @PostConstruct
    private void loadSubscribers() {
        subscribers = subscriberRepository.findAll();
        if (subscribers.isEmpty()) {
            throw new IllegalStateException("No subscribers found in the database. Initialize them first.");
        }
    }

    /**
     * Generates call data for the entire simulation period.
     * This method can be triggered via an API endpoint or a scheduled task.
     */
    public void generateCallsForYear() {
        // Generate a random number of calls per month for the year
        LocalDateTime currentMonthStart = SIMULATION_START_DATE;
        while (currentMonthStart.isBefore(SIMULATION_END_DATE) || currentMonthStart.isEqual(SIMULATION_END_DATE)) {
            int callsThisMonth = 50 + random.nextInt(150); // Random 50-199 calls per month
            generateCallsForPeriod(currentMonthStart, currentMonthStart.plusMonths(1).minusSeconds(1), callsThisMonth);
            currentMonthStart = currentMonthStart.plusMonths(1);
        }
        System.out.println("Finished generating all calls for the year.");
    }

    private void generateCallsForPeriod(LocalDateTime periodStart, LocalDateTime periodEnd, int numberOfCalls) {
        if (subscribers.isEmpty()) return;

        for (int i = 0; i < numberOfCalls; i++) {
            // Submit call generation to thread pool for parallel processing
            // However, saving to DB must be carefully managed to maintain chronological order if required strictly at write time.
            // For now, we generate and then sort before creating CDR files.
            executorService.submit(() -> generateAndSaveSingleCall(periodStart, periodEnd));
        }
    }

    @Transactional
    protected void generateAndSaveSingleCall(LocalDateTime periodStart, LocalDateTime periodEnd) {
        Collections.shuffle(subscribers); // Ensure randomness in caller/callee selection
        Subscriber caller = subscribers.get(random.nextInt(subscribers.size()));
        Subscriber callee;
        do {
            callee = subscribers.get(random.nextInt(subscribers.size()));
        } while (callee.getId().equals(caller.getId())); // Ensure caller and callee are different

        String callType = random.nextBoolean() ? "01" : "02"; // 01 outgoing, 02 incoming
        String msisdn = caller.getMsisdn();
        String peerMsisdn = callee.getMsisdn();

        if ("02".equals(callType)) { // Incoming call, swap msisdn and peerMsisdn
            msisdn = callee.getMsisdn();
            peerMsisdn = caller.getMsisdn();
        }

        long durationInSeconds = 10 + random.nextInt(3600); // Call duration between 10 seconds and 1 hour

        // Generate a random start time within the period
        long periodDurationSeconds = ChronoUnit.SECONDS.between(periodStart, periodEnd);
        if (periodDurationSeconds <= 0) return; // Avoid issues with invalid period

        LocalDateTime startTime = periodStart.plusSeconds(random.nextInt((int) periodDurationSeconds));
        LocalDateTime endTime = startTime.plusSeconds(durationInSeconds);

        // Ensure endTime does not exceed the simulation period end
        if (endTime.isAfter(SIMULATION_END_DATE)) {
            endTime = SIMULATION_END_DATE;
            // Recalculate duration if endTime was capped
            if (startTime.isAfter(endTime)) startTime = endTime.minusSeconds(10); // Ensure start is before end
        }
        if (startTime.isEqual(endTime)) endTime = startTime.plusSeconds(10); // Ensure duration is positive

        // Handle calls spanning midnight: if a call starts before midnight and ends after, split into two records.
        if (startTime.toLocalDate().isBefore(endTime.toLocalDate())) {
            // First record: from startTime to 23:59:59 of startTime's date
            LocalDateTime endOfFirstDay = startTime.with(LocalTime.MAX);
            CallDetailRecord record1 = new CallDetailRecord(null, callType, msisdn, peerMsisdn, startTime, endOfFirstDay, false);
            callDetailRecordRepository.save(record1);

            // Second record: from 00:00:00 of endTime's date to endTime
            LocalDateTime startOfSecondDay = endTime.with(LocalTime.MIN);
            CallDetailRecord record2 = new CallDetailRecord(null, callType, msisdn, peerMsisdn, startOfSecondDay, endTime, false);
            callDetailRecordRepository.save(record2);
        } else {
            CallDetailRecord record = new CallDetailRecord(null, callType, msisdn, peerMsisdn, startTime, endTime, false);
            callDetailRecordRepository.save(record);
        }
    }

    // For graceful shutdown
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 