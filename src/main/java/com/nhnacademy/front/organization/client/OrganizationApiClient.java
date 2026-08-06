package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.organization.dto.request.OrgStatusUpdateRequest;
import com.nhnacademy.front.organization.dto.request.OrgUpdateRequest;
import com.nhnacademy.front.organization.dto.request.OrganizationSetupRequest;
import com.nhnacademy.front.organization.dto.response.OrgDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 조직, 부서, 조직원
@Component
@RequiredArgsConstructor
public class OrganizationApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";

    /**
     * 조직 정보 조회
     */
    public OrgDetailResponse getOrgInfo() {
        return gatewayClient.get(CORE_SERVICE + "/organizations/me", OrgDetailResponse.class);
    }

    /**
     * 활성화, 비활성화
     */
    public void updateOrgStatus(OrgStatusUpdateRequest request) {
       gatewayClient.put(CORE_SERVICE + "/organizations/me/status", request);
    }

    /**
     * 조직 정보 수정
     */
    public void updateOrg(OrgUpdateRequest request) {
        gatewayClient.put(CORE_SERVICE + "/organizations/me", request);
    }

    /**
     * 조직 초기화
     */
    public void setupOrg(OrganizationSetupRequest request) {
        gatewayClient.put(CORE_SERVICE + "/organizations/me/setup", request);
    }
}
