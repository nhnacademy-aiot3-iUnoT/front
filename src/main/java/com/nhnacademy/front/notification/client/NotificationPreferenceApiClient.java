package com.nhnacademy.front.notification.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.notification.dto.request.NotificationChannelPreferencesUpdateRequest;
import com.nhnacademy.front.notification.dto.request.NotificationScopePreferenceRequest;
import com.nhnacademy.front.notification.dto.response.NotificationChannelPreferenceResponse;
import com.nhnacademy.front.notification.dto.response.NotificationScopePreferenceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 개인 알림 설정 (환경 알림 1:1 수신)
 */
@Component
@RequiredArgsConstructor
public class NotificationPreferenceApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core/organizations";

    private String path(Long organizationId) {
        return CORE_SERVICE + "/" + organizationId + "/notifications/preferences";
    }

    public List<NotificationChannelPreferenceResponse> getChannels(Long organizationId) {
        return gatewayClient.get(path(organizationId) + "/channels", new ParameterizedTypeReference<>() {});
    }

    public void updateChannels(Long organizationId, NotificationChannelPreferencesUpdateRequest request) {
        gatewayClient.put(path(organizationId) + "/channels", request);
    }

    public List<NotificationScopePreferenceResponse> getScopes(Long organizationId) {
        return gatewayClient.get(path(organizationId) + "/scopes", new ParameterizedTypeReference<>() {});
    }

    public void upsertScope(Long organizationId, NotificationScopePreferenceRequest request) {
        gatewayClient.put(path(organizationId) + "/scopes", request);
    }

    /**
     * 구역의 직접 설정을 지운다. 지우면 저장소 설정을 따르게 된다.
     */
    public void deleteScope(Long organizationId, Long storageId, Long zoneId) {
        StringBuilder uri = new StringBuilder(path(organizationId) + "/scopes?storage-id=" + storageId);

        if (zoneId != null) {
            uri.append("&zone-id=").append(zoneId);
        }

        gatewayClient.delete(uri.toString());
    }
}
