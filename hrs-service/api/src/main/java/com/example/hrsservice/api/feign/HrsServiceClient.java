package com.example.hrsservice.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.hrsservice.api.dto.TariffInfoHrsDto;

@FeignClient(name = "hrs-service", url = "${hrs.service.url}")
public interface HrsServiceClient {

    @GetMapping("/api/internal/tariffs/{id}")
    ResponseEntity<TariffInfoHrsDto> getTariffById(@PathVariable("id") Long id);
} 