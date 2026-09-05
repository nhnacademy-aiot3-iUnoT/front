package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.organization.dto.response.ZoneThresholdDetailResponse;
import com.nhnacademy.front.organization.dto.response.ZoneThresholdInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ZoneThresholdApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";

    public List<ZoneThresholdInfoResponse> getZoneThresholds(Long zoneId){
        String uri = String.format("%s/zones/%d/zone-thresholds", CORE_SERVICE, zoneId);

        return gatewayClient.get(
                uri,
                new ParameterizedTypeReference<>() {}
        );
    }

    public ZoneThresholdDetailResponse getZoneThreshold(Long zoneId, Long zoneThresholdId){
        String uri = String.format("%s/zones/%d/zone-thresholds/%d", CORE_SERVICE, zoneId, zoneThresholdId);

        return gatewayClient.get(
                uri,
                ZoneThresholdDetailResponse.class
        );
    }
}
