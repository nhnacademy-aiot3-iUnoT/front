package com.nhnacademy.front.inventory.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.inventory.dto.request.MedicineDisposalRequest;
import com.nhnacademy.front.inventory.dto.request.MedicineOutboundRequest;
import com.nhnacademy.front.inventory.dto.response.MedicineDisposalTargetResponse;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.inventory.dto.request.InboundMedicineRequest;
import com.nhnacademy.front.inventory.dto.response.InventoriesResponse;
import com.nhnacademy.front.inventory.dto.response.InventoryInfoResponse;
import com.nhnacademy.front.inventory.dto.response.MedicineOutboundTargetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

// 재고관리
@Component
@RequiredArgsConstructor
public class InventoryApiClient {
    private static final String CORE_SERVICE = "/api/core";
    private final GatewayClient gatewayClient;

    // 입고 등록
    public void inbound(InboundMedicineRequest request){
        gatewayClient.post(CORE_SERVICE + "/inventories",request);

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


        return gatewayClient.get(path,
                new ParameterizedTypeReference<>() {}
                );

    }



    // 상세 재고 조회
    public InventoryInfoResponse getInventoryInfo(Long storageId, Long packageUnitId,int page, int size){


        String path = UriComponentsBuilder
                .fromPath(CORE_SERVICE + "/inventories/storages/{storageId}/pack-units/{packageUnitId}")
                .queryParam("page", page)
                .queryParam("size", size)
                .buildAndExpand(storageId, packageUnitId)
                .encode()
                .toUriString();


        return gatewayClient.get(path,InventoryInfoResponse.class);

    }


    // 출고 대상 조회
    public MedicineOutboundTargetResponse getOutboundTarget(
            Long inventoryId
    ) {
        String path = UriComponentsBuilder
                .fromPath(
                        CORE_SERVICE
                                + "/medicine-inventories/{inventoryId}/outbound-target"
                )
                .buildAndExpand(inventoryId)
                .encode()
                .toUriString();

        return gatewayClient.get(
                path,
                MedicineOutboundTargetResponse.class
        );
    }

    // 출고 처리
    public void outbound(MedicineOutboundRequest request) {
        gatewayClient.post(
                CORE_SERVICE + "/medicine-inventories/outbound",
                request
        );
    }


    // 폐기 대상 조회
    public MedicineDisposalTargetResponse getDisposalTarget(Long inventoryId) {
        String path = UriComponentsBuilder
                .fromPath(CORE_SERVICE + "/inventories/{inventoryId}/disposal-target")
                .buildAndExpand(inventoryId)
                .encode()
                .toUriString();

        return gatewayClient.get(
                path,
                MedicineDisposalTargetResponse.class
        );
    }

    // 폐기 처리
    public void dispose(
            Long inventoryId,
            MedicineDisposalRequest request
    ) {
        String path = UriComponentsBuilder
                .fromPath(CORE_SERVICE + "/inventories/{inventoryId}/disposal")
                .buildAndExpand(inventoryId)
                .encode()
                .toUriString();

        gatewayClient.post(path, request);
    }
}
