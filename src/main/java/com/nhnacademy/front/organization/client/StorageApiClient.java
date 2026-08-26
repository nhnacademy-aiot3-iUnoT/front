package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.organization.dto.response.StorageDetailResponse;
import com.nhnacademy.front.organization.dto.response.StorageInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

// 저장소, 구역
@Component
@RequiredArgsConstructor
public class StorageApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";



    public List<StorageInfoResponse> getStorages(){
        String uri = String.format("%s/storages", CORE_SERVICE);

        return gatewayClient.get(
                uri,
                new ParameterizedTypeReference<>() {}
        );
    }

    public List<StorageInfoResponse> searchStorages(String name) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromPath(CORE_SERVICE + "/storages")
                .queryParam("name", name);

        return gatewayClient.get(builder.toUriString(), new ParameterizedTypeReference<>() {});
    }

    public StorageDetailResponse getStorage(Long storageId){
        String uri = String.format("%s/storages/%d", CORE_SERVICE, storageId);

        return gatewayClient.get(
                uri,
                StorageDetailResponse.class
        );
    }


    // 입고 - 저장소 목록
    public List<StorageInfoResponse> getStoragesInbound(){

        return gatewayClient.get(CORE_SERVICE + "/inbound/storages",
                new ParameterizedTypeReference<
                        ApiResponse<List<StorageInfoResponse>>
                        >() {});

    }



}
