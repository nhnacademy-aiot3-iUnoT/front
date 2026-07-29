package com.nhnacademy.front.admin.client;

import com.nhnacademy.front.global.client.GatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 관리자 페이지 구성을 위한
@Component
@RequiredArgsConstructor
public class AdminApiClient {
    private final GatewayClient backendApiClient;
    private static final String ACCOUNT_SERVICE = "/api/account";
    private static final String CORE_SERVICE = "/api/core";
}
