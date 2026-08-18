package com.nhnacademy.front.inventory.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.inventory.dto.response.StockThresholdInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StockThresholdApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";

    public List<StockThresholdInfoResponse> getStockThresholds(Long storageId){
        String uri = String.format("%s/storages/%d/stock-thresholds", CORE_SERVICE, storageId);

        return gatewayClient.get(
                uri,
                new ParameterizedTypeReference<ApiResponse<List<StockThresholdInfoResponse>>>() {}
        );
    }
}
