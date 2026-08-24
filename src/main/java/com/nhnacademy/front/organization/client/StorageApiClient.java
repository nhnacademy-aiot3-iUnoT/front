package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.organization.dto.response.StorageDetailResponse;
import com.nhnacademy.front.organization.dto.response.StorageInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

// 저장소, 구역
@Component
@RequiredArgsConstructor
public class StorageApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core"; // 담당자가 수정



    public List<StorageInfoResponse> getStorages(){
        String uri = String.format("%s/storages", CORE_SERVICE);

        return gatewayClient.get(
                uri,
                new ParameterizedTypeReference<ApiResponse<List<StorageInfoResponse>>>() {}
        );
    }

    public StorageDetailResponse getStorage(Long storageId){
        String uri = String.format("%s/storages/%d", CORE_SERVICE, storageId);

        return gatewayClient.get(
                uri,
                StorageDetailResponse.class
        );
    }
}
