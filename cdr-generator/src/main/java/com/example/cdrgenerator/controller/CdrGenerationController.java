package com.example.cdrgenerator.controller;

import com.example.cdrgenerator.service.CallGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cdr-generator")
@RequiredArgsConstructor
public class CdrGenerationController {

    private final CallGenerationService callGenerationService;

    @PostMapping("/generate-year")
    public ResponseEntity<String> generateCallsForYear() {
        callGenerationService.generateCallsForYear();
        return ResponseEntity.ok("Call generation for the year has been initiated.");
    }
} 