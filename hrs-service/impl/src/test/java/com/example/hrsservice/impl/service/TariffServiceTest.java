package com.example.hrsservice.impl.service;

import com.example.hrsservice.api.dto.TariffInfoHrsDto;
import com.example.hrsservice.impl.entity.Tariff;
import com.example.hrsservice.impl.repository.TariffRepository;
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
                1L, 2L, 0L,
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

        assertThrows(ResponseStatusException.class, () -> tariffService.getTariffInfoById(99L));
    }
} 