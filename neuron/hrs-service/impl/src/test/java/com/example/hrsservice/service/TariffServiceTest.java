package com.example.hrsservice.service;

import com.example.hrsservice.dto.TariffInfoHrsDto;
import com.example.hrsservice.entity.Tariff;
import com.example.hrsservice.repository.TariffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TariffServiceTest {

    @Mock
    private TariffRepository tariffRepository;

    @InjectMocks
    private TariffService tariffService;

    @Test
    void getTariffInfoById_ShouldReturnDto_WhenFound() {
        Tariff classicTariff = new Tariff(11L, "Классика", Tariff.TariffType.CLASSIC,
                BigDecimal.valueOf(1.50), BigDecimal.valueOf(2.50), BigDecimal.ZERO,
                null, null, null);
        when(tariffRepository.findById(11L)).thenReturn(Optional.of(classicTariff));

        TariffInfoHrsDto result = tariffService.getTariffInfoById(11L);

        assertNotNull(result);
        assertEquals(classicTariff.getId(), result.getId());
        assertEquals(classicTariff.getName(), result.getName());
        assertEquals("CLASSIC", result.getTariffType());
        assertEquals(classicTariff.getOutgoingCallRomashkaRate(), result.getOutgoingCallRomashkaRate());
    }

    @Test
    void getTariffInfoById_ShouldThrowNotFound_WhenNotFound() {
        when(tariffRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            tariffService.getTariffInfoById(99L);
        });
    }
} 