package com.nhnacademy.front.admin.client;

import com.nhnacademy.front.admin.dto.request.OrgCreateRequest;
import com.nhnacademy.front.admin.dto.request.OrgSearchRequest;
import com.nhnacademy.front.admin.dto.response.OrgCreateResponse;
import com.nhnacademy.front.admin.dto.response.OrgSearchResponse;
import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class AdminOrganizationClient {
    private final GatewayClient backendApiClient;
    private static final String CORE_SERVICE = "/api/core";

    /**
     * 조직 목록 조회 (status, name)
     */
    public PageResponse<OrgSearchResponse> getOrgList(OrgSearchRequest request, int page, int size) {

        UriComponentsBuilder builder = UriComponentsBuilder
        .fromPath(CORE_SERVICE + "/admin/organizations")
        .queryParam("page", page)
        .queryParam("size", size);

        if (request.status() != null) {
            builder.queryParam("status", request.status());
        }

        if (request.name() != null && !request.name().isBlank()) {
            builder.queryParam("name", request.status());
        }

        String uri = builder.toUriString();

        return backendApiClient.get(uri, new ParameterizedTypeReference<>(){});
    }

    /**
     * 조직 생성
     */
    public OrgCreateResponse createOrg(OrgCreateRequest request) {
        return backendApiClient.post(
                CORE_SERVICE + "/admin/organizations", request, OrgCreateResponse.class
        );
    }
}
