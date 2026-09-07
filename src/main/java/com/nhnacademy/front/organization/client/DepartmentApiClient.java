package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.ApiResponse;
import com.nhnacademy.front.organization.dto.request.DepartmentCreateRequest;
import com.nhnacademy.front.organization.dto.request.DepartmentStatusUpdateRequest;
import com.nhnacademy.front.organization.dto.request.DepartmentUpdateRequest;
import com.nhnacademy.front.organization.dto.response.DepartmentByStorageResponse;
import com.nhnacademy.front.organization.dto.response.DepartmentCreateResponse;
import com.nhnacademy.front.organization.dto.response.DepartmentInfoResponse;
import com.nhnacademy.front.organization.dto.response.DepartmentListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DepartmentApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core/departments";

    public DepartmentCreateResponse createDepartment(DepartmentCreateRequest request) {
        return gatewayClient.post(CORE_SERVICE, request, DepartmentCreateResponse.class);
    }

    public List<DepartmentListResponse> getDepartments() {
        return gatewayClient.get(CORE_SERVICE, new ParameterizedTypeReference<>() {});
    }

    public DepartmentInfoResponse getDepartment(Long departmentId) {
        return gatewayClient.get(CORE_SERVICE + "/" + departmentId, DepartmentInfoResponse.class);
    }

    public List<DepartmentListResponse> getMyDepartments() {
        return gatewayClient.get(CORE_SERVICE + "/me", new ParameterizedTypeReference<>() {});
    }

    public List<DepartmentByStorageResponse> getDepartmentsByStorageId(Long storageId){
        return gatewayClient.get(
                CORE_SERVICE + "/storages/" + storageId + "/departments",
                new ParameterizedTypeReference<>() {}
        );
    }

    public void updateDepartment(Long departmentId, DepartmentUpdateRequest request) {
        gatewayClient.put(CORE_SERVICE + "/" + departmentId, request);
    }

    public void updateDepartmentStatus(Long departmentId, DepartmentStatusUpdateRequest request) {
        gatewayClient.put(CORE_SERVICE + "/" + departmentId + "/status", request);
    }

    public void deleteDepartment(Long departmentId) {
        gatewayClient.delete(CORE_SERVICE + "/" + departmentId);
    }
}
