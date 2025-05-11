package com.example.brtservice.impl.listener;

import com.example.brtservice.impl.service.CdrProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CdrMessageListener {

    private final CdrProcessingService cdrProcessingService;

    public void receiveCdrFile(String cdrFileContent) {
        log.info("Received CDR file with {} bytes.", cdrFileContent.length());
        cdrProcessingService.processCdrFileContent(cdrFileContent);
    }
} 