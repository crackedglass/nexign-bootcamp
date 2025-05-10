package com.example.cdrgenerator.service;

import com.example.cdrgenerator.entity.CallDetailRecord;
import com.example.cdrgenerator.repository.CallDetailRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CdrFileService {

    private final CallDetailRecordRepository callDetailRecordRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name:cdr_exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key.name:cdr_routing_key}")
    private String routingKey;

    private static final int CDR_BATCH_SIZE = 10;

    /**
     * Periodically checks for unprocessed call records, groups them into CDR files,
     * sends them to RabbitMQ, and marks them as processed.
     * Runs every 1 minute, adjust as needed.
     */
    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    @Transactional
    public void processAndSendCdrFiles() {
        log.info("Starting CDR file processing and sending job.");
        List<CallDetailRecord> unprocessedRecords = callDetailRecordRepository.findByProcessedFalseOrderByStartTimeAsc();

        if (unprocessedRecords.isEmpty()) {
            log.info("No unprocessed call records found.");
            return;
        }

        log.info("Found {} unprocessed records. Grouping into CDR files of size {}.", unprocessedRecords.size(), CDR_BATCH_SIZE);

        for (int i = 0; i < unprocessedRecords.size(); i += CDR_BATCH_SIZE) {
            List<CallDetailRecord> batch = unprocessedRecords.subList(i, Math.min(i + CDR_BATCH_SIZE, unprocessedRecords.size()));
            
            if (batch.size() < CDR_BATCH_SIZE && unprocessedRecords.size() > i + CDR_BATCH_SIZE) {
                // This logic ensures that we only send full batches unless it's the very last set of records.
                // The spec says: "Каждые 10 записей составляют CDR". 
                // If we want to send partial CDRs for the remaining records at the end of a month or simulation, this needs adjustment.
                // For now, let's assume we always wait for 10 records, or it's the final batch.
                log.info("Skipping batch of size {} as it's not full and more records exist. Waiting for more records to form a full CDR.", batch.size());
                break; // Stop processing if a partial batch is encountered and more records are available later.
            }
            
            if (batch.isEmpty()) continue;

            // Records are already sorted by startTime due to the repository query.
            // The spec: "Данные в CDR идут не по порядку, т.е. записи по одному абоненту могут быть в разных частях файла; Хронология звонков при этом должна обязательно соблюдаться;"
            // This means the records *within one CDR file* must be chronological.
            // The overall stream of CDR files will also be chronological based on the first record in each CDR.
            String cdrFileContent = batch.stream()
                    .map(CallDetailRecord::toCdrString)
                    .collect(Collectors.joining("\n"));

            try {
                rabbitTemplate.convertAndSend(exchangeName, routingKey, cdrFileContent);
                log.info("Sent CDR file with {} records. First record start time: {}.", batch.size(), batch.get(0).getStartTime());

                // Mark records as processed
                for (CallDetailRecord record : batch) {
                    record.setProcessed(true);
                    callDetailRecordRepository.save(record);
                }
                log.info("Marked {} records as processed.", batch.size());
            } catch (Exception e) {
                log.error("Error sending CDR file to RabbitMQ or marking records as processed: {}", e.getMessage(), e);
                // Depending on requirements, implement retry logic or move to a dead-letter queue.
                // For now, we'll break and retry in the next scheduled run.
                break;
            }
        }
        log.info("Finished CDR file processing and sending job.");
    }
} 