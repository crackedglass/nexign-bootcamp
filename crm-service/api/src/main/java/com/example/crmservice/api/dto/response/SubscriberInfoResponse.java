package com.example.crmservice.api.dto.response;

import com.example.brtservice.api.dto.SubscriberDetailsBrtDto;
import com.example.hrsservice.api.dto.TariffInfoHrsDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberInfoResponse {
    private SubscriberDetailsBrtDto subscriberDetails;
    private TariffInfoHrsDto tariffInfo;
} 