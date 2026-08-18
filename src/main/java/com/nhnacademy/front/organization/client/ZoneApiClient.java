package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.organization.dto.response.ZoneDetailResponse;
import com.nhnacademy.front.organization.dto.response.ZoneInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ZoneApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";

    public List<ZoneInfoResponse> getZones(Long storageId){
        String uri = String.format("%s/storages/%d/zones", CORE_SERVICE, storageId);

        return gatewayClient.get(
                uri,
                new ParameterizedTypeReference<ApiResponse<List<ZoneInfoResponse>>>() {}
        );
    }

    public ZoneDetailResponse getZone(Long storageId, Long zoneId){
        String uri = String.format("%s/storages/%d/zones/%d", CORE_SERVICE, storageId, zoneId);

        return gatewayClient.get(
                uri,
                ZoneDetailResponse.class
        );
    }
}
