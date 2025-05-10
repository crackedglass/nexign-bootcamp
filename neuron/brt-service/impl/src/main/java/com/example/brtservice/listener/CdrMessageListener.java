package com.example.brtservice.listener;

import com.example.brtservice.service.CdrProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CdrMessageListener {

    private final CdrProcessingService cdrProcessingService;

    /**
     * Receives a CDR file content as a String from RabbitMQ.
     * @param cdrFileContent The content of the CDR file, with records separated by newlines.
     */
    public void receiveCdrFile(String cdrFileContent) {
        log.info("Received CDR file with {} bytes.", cdrFileContent.length());
        // Further processing will be handled by CdrProcessingService
        cdrProcessingService.processCdrFileContent(cdrFileContent);
    }
} 