package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.organization.dto.request.MemberByEmailRequest;
import com.nhnacademy.front.organization.dto.request.OrganizationMemberSearchRequest;
import com.nhnacademy.front.organization.dto.request.OrganizationRoleUpdateRequest;
import com.nhnacademy.front.organization.dto.response.OrganizationMemberResponse;
import com.nhnacademy.front.organization.dto.response.OrganizationMemberRoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrganizationMemberApiClient {
    private static final String CORE_SERVICE = "/api/core/members";

    private final GatewayClient gatewayClient;

    /**
     * 조직원 목록 (부서 배정 유무)
     */
    public PageResponse<OrganizationMemberResponse> getMembers(OrganizationMemberSearchRequest request, boolean withoutDepartment, int page, int size) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromPath(withoutDepartment ? CORE_SERVICE + "/without-department" : CORE_SERVICE)
                .queryParam("page", page)
                .queryParam("size", size);

        if (request.email() != null && !request.email().isBlank()) {
            builder.queryParam("email", request.email());
        }

        if (request.role() != null) {
            builder.queryParam("role", request.role());
        }

        String uri = builder.toUriString();
        return gatewayClient.get(uri, new ParameterizedTypeReference<>() {});
    }

    public List<OrganizationMemberResponse> searchMembers(MemberByEmailRequest request) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromPath(CORE_SERVICE + "/search")
                .queryParam("email", request.email());

        return gatewayClient.get(builder.toUriString(), new ParameterizedTypeReference<>() {});
    }

    public OrganizationMemberRoleResponse getRole(){
        String uri = String.format("%s/me/role", CORE_SERVICE);

        return gatewayClient.get(uri, OrganizationMemberRoleResponse.class);
    }

    /**
     * 조직원 Role 수정 (owner <-> member)
     */
    public void updateRole(Long memberId, OrganizationRoleUpdateRequest request) {
        gatewayClient.put(CORE_SERVICE + "/" + memberId + "/role", request);
    }

    /**
     * 조직원 삭제
     */
    public void deleteMember(Long memberId) {
        gatewayClient.delete(CORE_SERVICE + "/" + memberId);
    }

}
