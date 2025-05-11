package com.example.hrsservice.controller;

import com.example.hrsservice.dto.TariffInfoHrsDto; // DTO for HRS to return
import com.example.hrsservice.service.TariffService; // New service in HRS
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
public class HrsInternalControllerImpl implements HrsInternalController {

    private final TariffService tariffService;

    @Override
    public ResponseEntity<TariffInfoHrsDto> getTariffById(@PathVariable Long id) {
        TariffInfoHrsDto tariff = tariffService.getTariffInfoById(id);
        return ResponseEntity.ok(tariff);
    }
} 