package com.example.brtservice.api.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.brtservice.api.dto.NewSubscriberRequestBrtDto;
import com.example.brtservice.api.dto.SubscriberDetailsBrtDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/internal/subscribers") // For inter-service communication
@Tag(name = "BRT Internal Controller", description = "Internal API endpoints for subscriber billing and rating")
public interface BrtInternalController {
    @Operation(summary = "Create a new subscriber", description = "Creates a new subscriber in the billing and rating system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscriber created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<SubscriberDetailsBrtDto> createSubscriber(@Valid @RequestBody NewSubscriberRequestBrtDto request);

    @Operation(summary = "Get subscriber by MSISDN", description = "Retrieves subscriber details from the billing and rating system by MSISDN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscriber information retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Subscriber not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/msisdn/{msisdn}")
    public ResponseEntity<SubscriberDetailsBrtDto> getSubscriberByMsisdn(
        @Parameter(description = "MSISDN of the subscriber", required = true) @PathVariable String msisdn); 

    @Operation(summary = "Update subscriber tariff", description = "Updates the tariff plan for a subscriber in the billing system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tariff updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid tariff ID"),
        @ApiResponse(responseCode = "404", description = "Subscriber not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{msisdn}/tariff")
    public ResponseEntity<Void> updateSubscriberTariff(
        @Parameter(description = "MSISDN of the subscriber", required = true) @PathVariable String msisdn,
        @Parameter(description = "ID of the new tariff plan", required = true) @RequestParam Long newTariffId);

    @Operation(summary = "Top up subscriber balance", description = "Adds funds to a subscriber's balance in the billing system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balance topped up successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid amount"),
        @ApiResponse(responseCode = "404", description = "Subscriber not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{msisdn}/balance/top-up")
    public ResponseEntity<Void> topUpBalance(
        @Parameter(description = "MSISDN of the subscriber", required = true) @PathVariable String msisdn,
        @Parameter(description = "Amount to add to the balance", required = true) @RequestParam Long amount);
}