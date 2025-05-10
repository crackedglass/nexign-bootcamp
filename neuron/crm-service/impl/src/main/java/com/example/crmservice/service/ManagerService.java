package com.example.crmservice.service;

import com.example.brtservice.feign.BrtServiceClient;
import com.example.brtservice.dto.crm.NewSubscriberRequestBrtDto;
import com.example.brtservice.dto.crm.SubscriberDetailsBrtDto;
import com.example.crmservice.dto.request.NewSubscriberRequest;
import com.example.crmservice.dto.response.SubscriberInfoResponse;
import com.example.hrsservice.dto.TariffInfoHrsDto;
import com.example.hrsservice.feign.HrsServiceClient;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerService {

    private final BrtServiceClient brtServiceClient;
    private final HrsServiceClient hrsServiceClient;

    public SubscriberDetailsBrtDto createSubscriber(NewSubscriberRequest request) {
        log.info("Manager creating subscriber with MSISDN: {}", request.getMsisdn());
        // Validate tariffId with HRS first (optional, BRT might do this too)
        try {
            hrsServiceClient.getTariffById(request.getTariffId());
            log.info("Tariff ID {} validated with HRS.", request.getTariffId());
        } catch (FeignException.NotFound e) {
            log.error("Tariff ID {} not found in HRS.", request.getTariffId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tariff ID " + request.getTariffId() + " does not exist.");
        } catch (Exception e) {
            log.error("Error validating tariff ID {} with HRS: {}", request.getTariffId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error validating tariff with HRS.");
        }

        NewSubscriberRequestBrtDto brtRequest = new NewSubscriberRequestBrtDto(
                request.getMsisdn(),
                request.getFullName(),
                request.getTariffId(),
                // Spec: "баланс которого по умолчанию 100 у.е."
                // The DTO has a default, but BRT should ultimately ensure this if not provided or if it's a strict rule.
                request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.valueOf(100.00)
        );
        try {
            ResponseEntity<SubscriberDetailsBrtDto> response = brtServiceClient.createSubscriber(brtRequest);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Subscriber {} created successfully via BRT.", request.getMsisdn());
                return response.getBody();
            }
            log.error("Error creating subscriber {} via BRT. Status: {}, Body: {}", request.getMsisdn(), response.getStatusCode(), response.getBody());
            throw new ResponseStatusException(response.getStatusCode(), "BRT service error during subscriber creation.");
        } catch (FeignException e) {
            log.error("FeignException while creating subscriber {} via BRT: Status {}, Content: {}", request.getMsisdn(), e.status(), e.contentUTF8(), e);
            throw new ResponseStatusException(HttpStatus.valueOf(e.status()), "Error from BRT service: " + e.contentUTF8());
        } catch (Exception e) {
            log.error("Unexpected error creating subscriber {}: {}", request.getMsisdn(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error creating subscriber.");
        }
    }

    public SubscriberInfoResponse getSubscriberInfo(String msisdn) {
        log.info("Manager requesting info for MSISDN: {}", msisdn);
        SubscriberDetailsBrtDto subscriberDetails = null;
        try {
            ResponseEntity<SubscriberDetailsBrtDto> brtResponse = brtServiceClient.getSubscriberByMsisdn(msisdn);
            if (brtResponse.getStatusCode().is2xxSuccessful() && brtResponse.getBody() != null) {
                subscriberDetails = brtResponse.getBody();
                log.info("Fetched subscriber details for {} from BRT.", msisdn);
            } else {
                log.warn("Could not fetch subscriber details for {} from BRT. Status: {}, Body: {}", msisdn, brtResponse.getStatusCode(), brtResponse.getBody());
                throw new ResponseStatusException(brtResponse.getStatusCode(), "Could not retrieve subscriber details from BRT.");
            }
        } catch (FeignException.NotFound e) {
            log.warn("Subscriber with MSISDN {} not found in BRT.", msisdn);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscriber not found.");
        } catch (FeignException e) {
            log.error("FeignException while getting subscriber info for {} from BRT: Status {}, Content: {}", msisdn, e.status(), e.contentUTF8(), e);
            throw new ResponseStatusException(HttpStatus.valueOf(e.status()), "Error from BRT service: " + e.contentUTF8());
        }

        TariffInfoHrsDto tariffInfo = null;
        if (subscriberDetails != null && subscriberDetails.getTariffId() != null) {
            try {
                ResponseEntity<TariffInfoHrsDto> hrsResponse = hrsServiceClient.getTariffById(subscriberDetails.getTariffId());
                if (hrsResponse.getStatusCode().is2xxSuccessful() && hrsResponse.getBody() != null) {
                    tariffInfo = hrsResponse.getBody();
                    log.info("Fetched tariff info for tariff ID {} from HRS.", subscriberDetails.getTariffId());
                } else {
                    log.warn("Could not fetch tariff info for ID {} from HRS. Status: {}, Body: {}", subscriberDetails.getTariffId(), hrsResponse.getStatusCode(), hrsResponse.getBody());
                    // Don't fail the whole request if tariff info is missing, but log it.
                }
            } catch (FeignException e) {
                log.error("FeignException while getting tariff info for ID {} from HRS: Status {}, Content: {}", subscriberDetails.getTariffId(), e.status(), e.contentUTF8(), e);
                // Tariff info might be missing, proceed without it but log.
            }
        }
        return new SubscriberInfoResponse(subscriberDetails, tariffInfo);
    }

    public void changeTariff(String msisdn, Long newTariffId) {
        log.info("Manager changing tariff for MSISDN {} to {}", msisdn, newTariffId);
        // Validate newTariffId with HRS first
        try {
            hrsServiceClient.getTariffById(newTariffId);
            log.info("New tariff ID {} validated with HRS.", newTariffId);
        } catch (FeignException.NotFound e) {
            log.error("New tariff ID {} not found in HRS.", newTariffId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New Tariff ID " + newTariffId + " does not exist.");
        } catch (Exception e) {
            log.error("Error validating new tariff ID {} with HRS: {}", newTariffId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error validating tariff with HRS.");
        }

        try {
            ResponseEntity<Void> response = brtServiceClient.updateSubscriberTariff(msisdn, newTariffId);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Error changing tariff for {} in BRT. Status: {}", msisdn, response.getStatusCode());
                throw new ResponseStatusException(response.getStatusCode(), "BRT service error during tariff change.");
            }
            log.info("Tariff for MSISDN {} changed to {} successfully via BRT.", msisdn, newTariffId);
        } catch (FeignException e) {
            log.error("FeignException while changing tariff for {} in BRT: Status {}, Content: {}", msisdn, e.status(), e.contentUTF8(), e);
            if (e.status() == HttpStatus.NOT_FOUND.value()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscriber not found in BRT.");
            }
            throw new ResponseStatusException(HttpStatus.valueOf(e.status()), "Error from BRT service: " + e.contentUTF8());
        }
    }

    public void topUpBalance(String msisdn, BigDecimal amount) {
        log.info("Manager topping up balance for MSISDN {} by amount {}", msisdn, amount);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Top-up amount must be positive.");
        }
        try {
            ResponseEntity<Void> response = brtServiceClient.topUpBalance(msisdn, amount);
             if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Error topping up balance for {} in BRT. Status: {}", msisdn, response.getStatusCode());
                throw new ResponseStatusException(response.getStatusCode(), "BRT service error during balance top-up.");
            }
            log.info("Balance for MSISDN {} topped up by {} successfully via BRT.", msisdn, amount);
        } catch (FeignException e) {
            log.error("FeignException while topping up balance for {} in BRT: Status {}, Content: {}", msisdn, e.status(), e.contentUTF8(), e);
             if (e.status() == HttpStatus.NOT_FOUND.value()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscriber not found in BRT.");
            }
            throw new ResponseStatusException(HttpStatus.valueOf(e.status()), "Error from BRT service: " + e.contentUTF8());
        }
    }
} 