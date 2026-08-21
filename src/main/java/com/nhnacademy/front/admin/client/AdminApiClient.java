package com.nhnacademy.front.admin.client;

import com.nhnacademy.front.account.dto.request.UpdateAccountNameRequest;
import com.nhnacademy.front.admin.dto.request.AdminResetPasswordRequest;
import com.nhnacademy.front.admin.dto.request.AdminUserCreateRequest;
import com.nhnacademy.front.admin.dto.request.AdminUserStatusRequest;
import com.nhnacademy.front.admin.dto.response.AdminUserResponse;
import com.nhnacademy.front.global.client.GatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminApiClient {
    private final GatewayClient gatewayClient;
    private static final String ACCOUNT_SERVICE = "/api/accounts/admin";

    public List<AdminUserResponse> getUsers() {
        return gatewayClient.get(ACCOUNT_SERVICE, new ParameterizedTypeReference<>() {});
    }

    public AdminUserResponse getUser(UUID uuid) {
        return gatewayClient.get(ACCOUNT_SERVICE + "/" + uuid, AdminUserResponse.class);
    }

    public void createAdmin(AdminUserCreateRequest request) {
        gatewayClient.post(ACCOUNT_SERVICE, request);
    }

    public void updateName(UUID uuid, UpdateAccountNameRequest request) {
        gatewayClient.put(ACCOUNT_SERVICE + "/" + uuid, request);
    }

    public void updatePassword(UUID uuid, AdminResetPasswordRequest request) {
        gatewayClient.put(ACCOUNT_SERVICE + "/" + uuid + "/pwd", request);
    }

    public void changeStatus(UUID uuid, AdminUserStatusRequest request) {
        gatewayClient.put(ACCOUNT_SERVICE + "/" + uuid + "/status", request);
    }
}
