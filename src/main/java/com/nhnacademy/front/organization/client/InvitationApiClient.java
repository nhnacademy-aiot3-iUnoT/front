package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.organization.dto.request.InvitationCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.organization.dto.request.InvitationSearchRequest;
import com.nhnacademy.front.organization.dto.response.InvitationSearchResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class InvitationApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";

    public void verifyToken(String token) {
        gatewayClient.get(CORE_SERVICE + "/invitations/" + token);
    }

    public PageResponse<InvitationSearchResponse> getInvitations(InvitationSearchRequest request, int page, int size) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromPath(CORE_SERVICE + "/organizations/me/invitations")
                .queryParam("page", page)
                .queryParam("size", size);

        if (request.email() != null && !request.email().isBlank()) {
            builder.queryParam("email", request.email());
        }

        if (request.status() != null) {
            builder.queryParam("status", request.status());
        }

        return gatewayClient.get(builder.toUriString(), new ParameterizedTypeReference<>() {});
    }

    public void createInvitation(InvitationCreateRequest request) {
        gatewayClient.post(CORE_SERVICE + "/organizations/me/invitations", request);
    }

    public void resendInvitation(Long invitationId) {
        gatewayClient.post(CORE_SERVICE + "/organizations/me/invitations/" + invitationId + "/resend");
    }

    public void cancelInvitation(Long invitationId) {
        gatewayClient.delete(CORE_SERVICE + "/organizations/me/invitations/" + invitationId);
    }

    public void reissueInvitation(Long invitationId) {
        gatewayClient.post(CORE_SERVICE + "/organizations/me/invitations/" + invitationId + "/reissue");
    }
}
