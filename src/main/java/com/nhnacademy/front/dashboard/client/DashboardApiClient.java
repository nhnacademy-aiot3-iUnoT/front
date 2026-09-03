package com.nhnacademy.front.dashboard.client;

import com.nhnacademy.front.dashboard.dto.response.DashboardDepartmentsResponse;
import com.nhnacademy.front.dashboard.dto.response.DashboardEnvironmentResponse;
import com.nhnacademy.front.dashboard.dto.response.DashboardExpiringResponse;
import com.nhnacademy.front.dashboard.dto.response.DashboardSummaryResponse;
import com.nhnacademy.front.global.client.GatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardApiClient {

    private static final String CORE_SERVICE = "/api/core/dashboard";

    private final GatewayClient gatewayClient;

    // 해당 부서원이나 관리자가 접근할수있는 부서 조회
    public DashboardDepartmentsResponse getDepartments() {
        return gatewayClient.get(CORE_SERVICE + "/departments", DashboardDepartmentsResponse.class);
    }

    // 부서별 재고 요약정보 조회
    public DashboardSummaryResponse getSummary(Long departmentId) {
        return gatewayClient.get(
                CORE_SERVICE + "/summary" + departmentQuery(departmentId),
                DashboardSummaryResponse.class);
    }

    // 부서별 유통기한 임박 의약품 조회
    public DashboardExpiringResponse getExpiring(Long departmentId, int size) {
        String query = departmentId == null
                ? "?size=" + size
                : "?departmentId=" + departmentId + "&size=" + size;

        return gatewayClient.get(CORE_SERVICE + "/expiring" + query, DashboardExpiringResponse.class);
    }

    // 해당 저장소의 Status 조회
    public DashboardEnvironmentResponse getEnvironment(Long storageId) {
        return gatewayClient.get(
                CORE_SERVICE + "/environment?storageId=" + storageId,
                DashboardEnvironmentResponse.class);
    }

    private String departmentQuery(Long departmentId) {
        return departmentId == null ? "" : "?departmentId=" + departmentId;
    }
}
