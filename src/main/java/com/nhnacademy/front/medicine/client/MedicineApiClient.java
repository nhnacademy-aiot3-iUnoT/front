package com.nhnacademy.front.medicine.client;

import com.nhnacademy.front.global.client.GatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 의약품
@Component
@RequiredArgsConstructor
public class MedicineApiClient {
    private final GatewayClient backendApiClient;
    private static final String CORE_SERVICE = "/api/core"; // 담당자가 수정
}
