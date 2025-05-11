package com.example.hrsservice.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hrsservice.api.dto.TariffInfoHrsDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/internal/tariffs")
@Tag(name = "HRS Internal Controller", description = "Internal API endpoints for tariff information")
public interface HrsInternalController {
    
    @Operation(summary = "Get tariff by ID", description = "Retrieves tariff information by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tariff information retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Tariff not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TariffInfoHrsDto> getTariffById(
        @Parameter(description = "ID of the tariff", required = true) @PathVariable Long id);
}
