package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 조직, 부서, 조직원
@Component
@RequiredArgsConstructor
public class OrganizationApiClient {
    private final GatewayClient backendApiClient;
    private static final String CORE_SERVICE = "/api/core"; // 담당자가 수정
}
