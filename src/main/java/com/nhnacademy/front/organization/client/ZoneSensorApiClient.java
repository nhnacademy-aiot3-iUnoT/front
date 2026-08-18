package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.organization.dto.response.ZoneSensorDetailResponse;
import com.nhnacademy.front.organization.dto.response.ZoneSensorInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ZoneSensorApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";

    public List<ZoneSensorInfoResponse> getZoneThresholds(Long zoneId){
        String uri = String.format("%s/zones/%d/zone-sensors", CORE_SERVICE, zoneId);

        return gatewayClient.get(
                uri,
                new ParameterizedTypeReference<ApiResponse<List<ZoneSensorInfoResponse>>>() {}
        );
    }

    public ZoneSensorDetailResponse getZoneThreshold(Long zoneId, Long zoneSensorId){
        String uri = String.format("%s/zones/%d/zone-sensors/%d", CORE_SERVICE, zoneId, zoneSensorId);

        return gatewayClient.get(
                uri,
                ZoneSensorDetailResponse.class
        );
    }
}
