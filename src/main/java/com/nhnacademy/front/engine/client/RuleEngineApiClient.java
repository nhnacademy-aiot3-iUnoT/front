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

    private static final String RULE_ENGINE_SERVICE = "/api/rule-engine";

    private final GatewayClient gatewayClient;

    // zoneId의 각 센서별 최신데이터
    public List<SensorLatestResponse> getLatestSensors(Long organizationId, Long zoneId) {
        String path = sensorDataPath(organizationId, zoneId) + "/latest";

        return gatewayClient.get(
                path,
                new ParameterizedTypeReference<
                        ApiResponse<List<SensorLatestResponse>>
                        >() {
                }
        );
    }

    // zoneId의 센서별 1일 데이터
    public List<SensorHistoryResponse> getSensorHistory(Long organizationId, Long zoneId) {
        String path = sensorDataPath(organizationId, zoneId) + "/history";

        return gatewayClient.get(
                path,
                new ParameterizedTypeReference<
                        ApiResponse<List<SensorHistoryResponse>>
                        >() {
                }
        );
    }

    // 조직의 가상 센서 목록
    public List<VirtualSensorInfoResponse> getVirtualSensors(Long organizationId) {
        return gatewayClient.get(
                virtualSensorPath(organizationId),
                new ParameterizedTypeReference<
                        ApiResponse<List<VirtualSensorInfoResponse>>
                        >() {
                }
        );
    }

    // 가상 센서데이터 생성
    public VirtualSensorCreateResponse createVirtualSensorData(
            Long organizationId,
            VirtualSensorCreateRequest request
    ) {
        return gatewayClient.post(
                virtualSensorPath(organizationId),
                request,
                VirtualSensorCreateResponse.class
        );
    }

    // 가상 센서데이터 수정
    public VirtualSensorUpdateResponse updateVirtualSensorData(
            Long organizationId,
            String deviceEui,
            VirtualSensorUpdateRequest request
    ) {
        return gatewayClient.put(
                virtualSensorPath(organizationId, deviceEui),
                request,
                VirtualSensorUpdateResponse.class
        );
    }

    // 가상 센서데이터 삭제
    public void deleteVirtualSensorData(
            Long organizationId,
            String deviceEui
    ) {
        gatewayClient.delete(virtualSensorPath(organizationId, deviceEui));
    }

    // 가상 센서데이터 설정 조회
    public VirtualSensorInfoResponse getVirtualSensorData(
            Long organizationId,
            String deviceEui
    ) {
        return gatewayClient.get(
                virtualSensorPath(organizationId, deviceEui),
                VirtualSensorInfoResponse.class
        );
    }

    // 가상 센서데이터 활성화 / 비활성화
    public void changeVirtualSensorStatus(
            Long organizationId,
            String deviceEui,
            VirtualSensorStatusRequest request
    ) {
        gatewayClient.put(virtualSensorPath(organizationId, deviceEui) + "/status", request);
    }

    // 센서 데이터 조회는 저장소를 거치지 않고 조직 + 구역으로만 찾는다.
    private String sensorDataPath(
            Long organizationId,
            Long zoneId
    ) {
        return RULE_ENGINE_SERVICE
                + "/organizations/" + organizationId
                + "/zones/" + zoneId
                + "/sensor-data";
    }

    private String virtualSensorPath(Long organizationId) {
        return RULE_ENGINE_SERVICE
                + "/organizations/" + organizationId
                + "/virtual-sensors";
    }

    private String virtualSensorPath(Long organizationId, String deviceEui) {
        return virtualSensorPath(organizationId) + "/" + deviceEui;
    }
}
