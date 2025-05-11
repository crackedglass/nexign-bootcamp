package com.example.hrsservice.impl.controller;

import com.example.hrsservice.api.dto.TariffInfoHrsDto;
import com.example.hrsservice.impl.service.TariffService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class HrsInternalControllerImpl {

    private final TariffService tariffService;

    @GetMapping("/tariffs/{id}")
    public TariffInfoHrsDto getTariffInfo(@PathVariable Long id) {
        return tariffService.getTariffInfo(id);
    }
} 