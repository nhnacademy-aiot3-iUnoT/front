package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.organization.dto.response.InvitationVerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvitationApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";

    public InvitationVerifyResponse verifyToken(String token) {
        return gatewayClient.get(CORE_SERVICE + "/invitations/" + token, InvitationVerifyResponse.class);
    }
}
