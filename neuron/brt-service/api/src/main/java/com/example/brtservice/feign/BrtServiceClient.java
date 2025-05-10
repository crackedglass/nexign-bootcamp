package com.example.brtservice.feign;

import com.example.brtservice.dto.crm.SubscriberDetailsBrtDto;
import com.example.brtservice.dto.crm.NewSubscriberRequestBrtDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

// Name of the Feign client, and URL from application.properties or service discovery
@FeignClient(name = "brt-service", url = "${brt.service.url}")
public interface BrtServiceClient {

    @PostMapping("/api/internal/subscribers")
    ResponseEntity<SubscriberDetailsBrtDto> createSubscriber(@RequestBody NewSubscriberRequestBrtDto request);

    @GetMapping("/api/internal/subscribers/msisdn/{msisdn}")
    ResponseEntity<SubscriberDetailsBrtDto> getSubscriberByMsisdn(@PathVariable("msisdn") String msisdn);

    @PutMapping("/api/internal/subscribers/{msisdn}/tariff")
    ResponseEntity<Void> updateSubscriberTariff(@PathVariable("msisdn") String msisdn, @RequestParam("newTariffId") Long newTariffId);

    @PostMapping("/api/internal/subscribers/{msisdn}/balance/top-up")
    ResponseEntity<Void> topUpBalance(@PathVariable("msisdn") String msisdn, @RequestParam("amount") BigDecimal amount);

}
