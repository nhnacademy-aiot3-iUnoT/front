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

/**
 * 센서 데이터와 가상 센서는 룰엔진이 갖고 있지만 요청은 인벤토리를 거친다.
 * 구역이 어느 조직 것인지 아는 쪽이 인벤토리뿐이라 접근 검증을 거기서 하기 때문이다.
 * 조직 ID는 인벤토리가 토큰으로 해석하므로 보내지 않는다.
 */
@Component
@RequiredArgsConstructor
public class RuleEngineApiClient {

    private static final String CORE_SERVICE = "/api/core";

    private final GatewayClient gatewayClient;

    // zoneId의 각 센서별 최신데이터
    public List<SensorLatestResponse> getLatestSensors(Long zoneId) {
        String path = zonePath(zoneId) + "/sensor-data/latest";

        return gatewayClient.get(
                path,
                new ParameterizedTypeReference<
                        ApiResponse<List<SensorLatestResponse>>
                        >() {
                }
        );
    }

    // zoneId의 센서별 1일 데이터
    public List<SensorHistoryResponse> getSensorHistory(Long zoneId) {
        String path = zonePath(zoneId) + "/sensor-data/history";

        return gatewayClient.get(
                path,
                new ParameterizedTypeReference<
                        ApiResponse<List<SensorHistoryResponse>>
                        >() {
                }
        );
    }

    // 가상 센서데이터 생성
    public VirtualSensorCreateResponse createVirtualSensorData(
            Long zoneId,
            VirtualSensorCreateRequest request
    ) {
        return gatewayClient.post(
                virtualSensorPath(zoneId),
                request,
                VirtualSensorCreateResponse.class
        );
    }

    // 가상 센서데이터 수정
    public VirtualSensorUpdateResponse updateVirtualSensorData(
            Long zoneId,
            VirtualSensorUpdateRequest request
    ) {
        return gatewayClient.put(
                virtualSensorPath(zoneId),
                request,
                VirtualSensorUpdateResponse.class
        );
    }

    // 가상 센서데이터 삭제
    public void deleteVirtualSensorData(Long zoneId) {
        gatewayClient.delete(virtualSensorPath(zoneId));
    }

    // 가상 센서데이터 설정 조회
    public VirtualSensorInfoResponse getVirtualSensorData(Long zoneId) {
        return gatewayClient.get(
                virtualSensorPath(zoneId),
                VirtualSensorInfoResponse.class
        );
    }

    // 가상 센서데이터 활성화 / 비활성화
    public void changeVirtualSensorStatus(
            Long zoneId,
            VirtualSensorStatusRequest request
    ) {
        gatewayClient.put(virtualSensorPath(zoneId) + "/status", request);
    }

    private String virtualSensorPath(Long zoneId) {
        return zonePath(zoneId) + "/virtual-sensor";
    }

    private String zonePath(Long zoneId) {
        return CORE_SERVICE + "/zones/" + zoneId;
    }
}
