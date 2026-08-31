package com.nhnacademy.front.inventory.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.inventory.dto.TransactionType;
import com.nhnacademy.front.inventory.dto.response.StockTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;

// 재고 변동 내역
@Component
@RequiredArgsConstructor
public class StockTransactionApiClient {

    private static final String CORE_SERVICE = "/api/core";

    private final GatewayClient gatewayClient;

    public PageResponse<StockTransactionResponse> search(
            Long zoneId,
            String medicineName,
            TransactionType transactionType,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    ) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromPath(CORE_SERVICE + "/zones/{zoneId}/stock-transaction")
                .queryParam("page", page)
                .queryParam("size", size);

        if (medicineName != null && !medicineName.isBlank()) {
            uriBuilder.queryParam("medicineName", medicineName.trim());
        }

        if (transactionType != null) {
            uriBuilder.queryParam("transactionType", transactionType.name());
        }

        if (startDate != null) {
            uriBuilder.queryParam("startDate", startDate);
        }

        if (endDate != null) {
            uriBuilder.queryParam("endDate", endDate);
        }

        String path = uriBuilder
                .buildAndExpand(zoneId)
                .encode()
                .toUriString();

        return gatewayClient.get(path, new ParameterizedTypeReference<>() {});
    }
}
