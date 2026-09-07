package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.organization.dto.request.DepartmentTelegramChatRegisterRequest;
import com.nhnacademy.front.organization.dto.response.DepartmentTelegramChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 부서 단톡방(텔레그램 챗봇) 연결
 */
@Component
@RequiredArgsConstructor
public class DepartmentTelegramChatApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core/departments";

    private String path(Long departmentId) {
        return CORE_SERVICE + "/" + departmentId + "/telegram-chat";
    }

    /**
     * 연결된 단톡방 조회. 연결이 없으면 null이다.
     */
    public DepartmentTelegramChatResponse getTelegramChat(Long departmentId) {
        return gatewayClient.get(path(departmentId), DepartmentTelegramChatResponse.class);
    }

    public void registerTelegramChat(Long departmentId, DepartmentTelegramChatRegisterRequest request) {
        gatewayClient.put(path(departmentId), request);
    }

    public void unlinkTelegramChat(Long departmentId) {
        gatewayClient.delete(path(departmentId));
    }
}
