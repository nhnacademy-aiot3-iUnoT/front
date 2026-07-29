package com.nhnacademy.front.inventory.client;

import com.nhnacademy.front.global.client.GatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 재고관리
@Component
@RequiredArgsConstructor
public class InventoryApiClient {
    private final GatewayClient backendApiClient;
    private static final String CORE_SERVICE = "/api/core"; // 담당자가 수정
}
