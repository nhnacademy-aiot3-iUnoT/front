package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.organization.dto.response.StorageDepartmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StorageDepartmentApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";

    /**
     * 부서 담당 저장소 목록
     */
    public List<StorageDepartmentResponse> getStorages(Long departmentId) {
        return gatewayClient.get(
                CORE_SERVICE + "/departments/" + departmentId + "/storages",
                new ParameterizedTypeReference<>() {}
        );
    }

    /**
     * 부서에 저장소 등록
     */
    public void addStorage(Long departmentId, Long storageId) {
        gatewayClient.post(CORE_SERVICE + "/departments/" + departmentId + "/storages/" + storageId);
    }

    /**
     * 부서에서 저장소 삭제
     */
    public void removeStorage(Long departmentId, Long storageId) {
        gatewayClient.delete(CORE_SERVICE + "/departments/" + departmentId + "/storages/" + storageId);
    }
}
