package com.nhnacademy.front.inventory.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.inventory.dto.request.InboundMedicineRequest;
import com.nhnacademy.front.inventory.dto.response.InventoriesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

// 재고관리
@Component
@RequiredArgsConstructor
public class InventoryApiClient {
    private final GatewayClient backendApiClient;
    private static final String CORE_SERVICE = "/api/core"; // 담당자가 수정


    // 입고 등록
    public void inbound(InboundMedicineRequest request){
        backendApiClient.post(CORE_SERVICE + "/inventories",request);

    }


    // 전체 재고 조회
    public PageResponse<InventoriesResponse> getInventories(String search, Long storageId, int page, int size){

        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder
                        .fromPath(CORE_SERVICE + "/inventories")
                        .queryParam("page", page)
                        .queryParam("size", size);

        if (search != null && !search.isBlank()) {
            uriBuilder.queryParam("search", search.trim());
        }

        if (storageId != null) {
            uriBuilder.queryParam("storage-id", storageId);
        }

        String path = uriBuilder
                .build()
                .encode()
                .toUriString();


        return backendApiClient.get(path,
                new ParameterizedTypeReference<>() {}
                );

    }


    // 출고


    // 폐기













}
