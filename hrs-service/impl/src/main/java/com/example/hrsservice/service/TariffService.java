package com.example.hrsservice.service;

import com.example.hrsservice.dto.TariffInfoHrsDto;
import com.example.hrsservice.entity.Tariff;
import com.example.hrsservice.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class TariffService {

    private final TariffRepository tariffRepository;

    public TariffInfoHrsDto getTariffInfoById(Long id) {
        log.debug("Fetching tariff by ID: {}", id);
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Tariff not found by ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Tariff not found");
                });
        return mapToInfoDto(tariff);
    }

    private TariffInfoHrsDto mapToInfoDto(Tariff tariff) {
        return new TariffInfoHrsDto(
                tariff.getId(),
                tariff.getName(),
                tariff.getTariffType().name(), // Enum to String
                tariff.getOutgoingCallRomashkaRate(),
                tariff.getOutgoingCallOtherRate(),
                tariff.getIncomingCallRate(),
                tariff.getMonthlyFee(),
                tariff.getIncludedMinutes(),
                tariff.getFallbackTariffId()
        );
    }
} 