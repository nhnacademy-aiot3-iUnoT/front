package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.organization.dto.response.ThresholdSpecResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ThresholdZoneApiClient {

    private final  GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";


    public List<ThresholdSpecResponse> getThresholdZones(Long zoneId){

        return gatewayClient.get(CORE_SERVICE + "/internal/zones/" + zoneId + "/zone-threshold",
                new ParameterizedTypeReference<>() {}
                );

    }



}
