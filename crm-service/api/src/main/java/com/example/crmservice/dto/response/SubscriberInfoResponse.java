package com.example.crmservice.dto.response;

import com.example.brtservice.dto.crm.SubscriberDetailsBrtDto;
import com.example.hrsservice.dto.TariffInfoHrsDto;

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