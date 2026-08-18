package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.organization.dto.response.SensorTypeInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SensorTypeApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";

    public List<SensorTypeInfoResponse> getSensorTypes(){
        String uri = String.format("%s/sensor-types", CORE_SERVICE);

        return gatewayClient.get(
                uri,
                new ParameterizedTypeReference<ApiResponse<List<SensorTypeInfoResponse>>> () {}
        );
    }
}
