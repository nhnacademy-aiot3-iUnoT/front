package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.organization.dto.response.SensorHistoryResponse;
import com.nhnacademy.front.organization.dto.response.SensorLatestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RuleEngineApiClient {

    private static final String CORE_SERVICE =
            "/api/rule-engine";

    private final GatewayClient gatewayClient;

    // zoneId의 각 센서별 최신데이터
    public List<SensorLatestResponse> getLatestSensors(
            Long zoneId
    ) {
        String path = CORE_SERVICE
                + "/zones/" + zoneId
                + "/sensor-data/latest";

        return gatewayClient.get(
                path,
                new ParameterizedTypeReference<
                        ApiResponse<List<SensorLatestResponse>>
                        >() {
                }
        );
    }

    // zoneId의 센서별 1일 데이터
    public List<SensorHistoryResponse> getSensorHistory(
            Long zoneId
    ) {
        String path = CORE_SERVICE
                + "/zones/" + zoneId
                + "/sensor-data/history";
        return gatewayClient.get(path,
                new ParameterizedTypeReference<
                        ApiResponse<List<SensorHistoryResponse>>
                        >() {
                }
        );
    }
}