package com.example.crmservice.impl.mapper;

import com.example.brtservice.api.dto.SubscriberDetailsBrtDto;
import com.example.crmservice.impl.domain.NewSubscriber;
import com.example.crmservice.impl.domain.SubscriberDetails;
import com.example.crmservice.api.dto.request.NewSubscriberRequest;
import com.example.crmservice.api.dto.response.SubscriberDetailsCrmDto;
import org.springframework.stereotype.Component;

@Component
public class SubscriberMapper {
    
    public NewSubscriber toDomain(NewSubscriberRequest request) {
        return new NewSubscriber(
            request.getMsisdn(),
            request.getFullName(),
            request.getTariffId(),
            request.getInitialBalance()
        );
    }

    public SubscriberDetailsCrmDto toDto(SubscriberDetails details) {
        return new SubscriberDetailsCrmDto(
            details.getId(),
            details.getFullName(),
            details.getMsisdn(),
            details.getBalance(),
            details.getRegistrationDate(),
            details.getTariffId()
        );
    }

    public SubscriberDetails fromBrtDto(SubscriberDetailsBrtDto dto) {
        return new SubscriberDetails(
            dto.getId(),
            dto.getFullName(),
            dto.getMsisdn(),
            dto.getBalance(),
            dto.getRegistrationDate(),
            dto.getTariffId()
        );
    }
} 