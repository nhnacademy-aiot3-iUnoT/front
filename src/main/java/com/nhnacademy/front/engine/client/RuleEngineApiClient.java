package com.nhnacademy.front.engine.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.engine.dto.request.VirtualSensorCreateRequest;
import com.nhnacademy.front.engine.dto.request.VirtualSensorStatusRequest;
import com.nhnacademy.front.engine.dto.request.VirtualSensorUpdateRequest;
import com.nhnacademy.front.engine.dto.response.SensorHistoryResponse;
import com.nhnacademy.front.engine.dto.response.SensorLatestResponse;
import com.nhnacademy.front.engine.dto.response.VirtualSensorCreateResponse;
import com.nhnacademy.front.engine.dto.response.VirtualSensorInfoResponse;
import com.nhnacademy.front.engine.dto.response.VirtualSensorUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RuleEngineApiClient {

    private static final String RULE_SERVICE =
            "/api/rule-engine";

    private final GatewayClient gatewayClient;

    // zoneId의 각 센서별 최신데이터
    public List<SensorLatestResponse> getLatestSensors(
            Long zoneId
    ) {
        String path = RULE_SERVICE
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
        String path = RULE_SERVICE
                + "/zones/" + zoneId
                + "/sensor-data/history";
        return gatewayClient.get(path,
                new ParameterizedTypeReference<
                        ApiResponse<List<SensorHistoryResponse>>
                        >() {
                }
        );
    }


    // 가상 센서데이터 생성
    public VirtualSensorCreateResponse createVirtualSensorData(
            Long organizationId,
            Long storageId,
            Long zoneId,
            VirtualSensorCreateRequest request
    ) {
        return gatewayClient.post(
                virtualSensorPath(organizationId, storageId, zoneId),
                request,
                VirtualSensorCreateResponse.class
        );
    }

    // 가상 센서데이터 수정
    public VirtualSensorUpdateResponse updateVirtualSensorData(
            Long organizationId,
            Long storageId,
            Long zoneId,
            VirtualSensorUpdateRequest request
    ) {
        return gatewayClient.put(
                virtualSensorPath(organizationId, storageId, zoneId),
                request,
                VirtualSensorUpdateResponse.class
        );
    }

    // 가상 센서데이터 삭제
    public void deleteVirtualSensorData(
            Long organizationId,
            Long storageId,
            Long zoneId
    ) {
        gatewayClient.delete(virtualSensorPath(organizationId, storageId, zoneId));
    }

    // 가상 센서데이터 설정 조회
    public VirtualSensorInfoResponse getVirtualSensorData(
            Long organizationId,
            Long storageId,
            Long zoneId
    ) {
        return gatewayClient.get(
                virtualSensorPath(organizationId, storageId, zoneId),
                VirtualSensorInfoResponse.class
        );
    }

    // 가상 센서데이터 활성화 / 비활성화
    public void changeVirtualSensorStatus(
            Long organizationId,
            Long storageId,
            Long zoneId,
            VirtualSensorStatusRequest request
    ) {
        String path = zonePath(organizationId, storageId, zoneId) + "/status";

        gatewayClient.put(path, request);
    }

    private String virtualSensorPath(
            Long organizationId,
            Long storageId,
            Long zoneId
    ) {
        return zonePath(organizationId, storageId, zoneId) + "/virtual-sensor";
    }

    private String zonePath(
            Long organizationId,
            Long storageId,
            Long zoneId
    ) {
        return RULE_SERVICE
                + "/organizations/" + organizationId
                + "/storages/" + storageId
                + "/zones/" + zoneId;
    }

}
