package com.example.crmservice.api.controller;

import com.example.crmservice.api.dto.request.ChangeTariffRequest;
import com.example.crmservice.api.dto.request.NewSubscriberRequest;
import com.example.crmservice.api.dto.request.TopUpBalanceRequest;
import com.example.crmservice.api.dto.response.SubscriberDetailsCrmDto;
import com.example.crmservice.api.dto.response.SubscriberInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager")
@Tag(name = "Manager Controller", description = "API endpoints for managing subscribers")
public interface ManagerController {
    
    @Operation(summary = "Create a new subscriber", description = "Creates a new subscriber with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscriber created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/subscribers")
    public ResponseEntity<SubscriberDetailsCrmDto> createSubscriber(@Valid @RequestBody NewSubscriberRequest request);

    @Operation(summary = "Get subscriber information", description = "Retrieves detailed information about a subscriber by their MSISDN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscriber information retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Subscriber not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/subscribers/{msisdn}/info")
    public ResponseEntity<SubscriberInfoResponse> getSubscriberInfo(
        @Parameter(description = "MSISDN of the subscriber", required = true) @PathVariable String msisdn);

    @Operation(summary = "Change subscriber tariff", description = "Updates the tariff plan for a subscriber")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tariff changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Subscriber not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/subscribers/tariff")
    public ResponseEntity<Void> changeTariff(@Valid @RequestBody ChangeTariffRequest request);

    @Operation(summary = "Top up subscriber balance", description = "Adds funds to a subscriber's balance")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balance topped up successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Subscriber not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/subscribers/balance/top-up")
    public ResponseEntity<Void> topUpBalance(@Valid @RequestBody TopUpBalanceRequest request);
}