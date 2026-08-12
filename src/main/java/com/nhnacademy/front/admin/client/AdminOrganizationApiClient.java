package com.nhnacademy.front.admin.client;

import com.nhnacademy.front.admin.dto.request.OrgCreateRequest;
import com.nhnacademy.front.admin.dto.request.OrgSearchRequest;
import com.nhnacademy.front.admin.dto.response.OrgCreateResponse;
import com.nhnacademy.front.admin.dto.response.AdminOrgDetailResponse;
import com.nhnacademy.front.admin.dto.response.OrgSearchResponse;
import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class AdminOrganizationApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE_ADMIN = "/api/core/admin/organizations";

    /**
     * 조직 목록 조회 (status, name)
     */
    public PageResponse<OrgSearchResponse> getOrgList(OrgSearchRequest request, int page, int size) {

        UriComponentsBuilder builder = UriComponentsBuilder
            .fromPath(CORE_SERVICE_ADMIN)
            .queryParam("page", page)
            .queryParam("size", size);

        if (request.status() != null) {
            builder.queryParam("status", request.status());
        }

        if (request.name() != null && !request.name().isBlank()) {
            builder.queryParam("name", request.name());
        }

        String uri = builder.toUriString();

        return gatewayClient.get(uri, new ParameterizedTypeReference<>(){});
    }

    /**
     * 조직 생성
     */
    public OrgCreateResponse createOrg(OrgCreateRequest request) {
        return gatewayClient.post(CORE_SERVICE_ADMIN, request, OrgCreateResponse.class);
    }

    /**
     * 조직 상세 조회
     */
    public AdminOrgDetailResponse getOrgDetail(Long id) {
        return gatewayClient.get(CORE_SERVICE_ADMIN + "/" + id, AdminOrgDetailResponse.class);
    }

    /**
     * 조직 삭제
     */
    public void deleteOrg(Long id) {
        gatewayClient.delete(CORE_SERVICE_ADMIN + "/" + id);
    }

    /**
     * Owner 초대 재전송
     */
    public void resendInvitation(Long organizationId, Long invitationId) {
        gatewayClient.post(CORE_SERVICE_ADMIN + "/" + organizationId + "/invitations/" + invitationId + "/resend");
    }

    public void cancelInvitation(Long organizationId, Long invitationId) {
        gatewayClient.delete(CORE_SERVICE_ADMIN + "/" + organizationId + "/invitations/" + invitationId);
    }

    public void reissueInvitation(Long organizationId, Long invitationId) {
        gatewayClient.post(CORE_SERVICE_ADMIN + "/" + organizationId + "/invitations/" + invitationId + "/reissue");
    }
}
