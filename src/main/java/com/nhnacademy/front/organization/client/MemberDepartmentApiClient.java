package com.nhnacademy.front.organization.client;

import com.nhnacademy.front.global.client.GatewayClient;
import com.nhnacademy.front.organization.dto.request.MemberDepartmentAssignRequest;
import com.nhnacademy.front.organization.dto.response.OrganizationMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 조직원_부서
 */
@Component
@RequiredArgsConstructor
public class MemberDepartmentApiClient {
    private final GatewayClient gatewayClient;
    private static final String CORE_SERVICE = "/api/core";

    /**
     * 부서에 속한 조직원 목록
     */
    public List<OrganizationMemberResponse> getMembers(Long departmentId) {
        return gatewayClient.get(
                CORE_SERVICE + "/departments/" + departmentId + "/members",
                new ParameterizedTypeReference<>() {}
        );
    }

    /**
     * 부서에 조직원 추가/삭제
     */
    public void addMember(Long departmentId, Long memberId) {
        gatewayClient.post(CORE_SERVICE + "/departments/" + departmentId + "/members/" + memberId);
    }

    public void removeMember(Long departmentId, Long memberId) {
        gatewayClient.delete(CORE_SERVICE + "/departments/" + departmentId + "/members/" + memberId);
    }

    /**
     * 조직원 부서 배정 (한번에 여러개)
     */
    public void assignMemberDepartments(Long memberId, MemberDepartmentAssignRequest request) {
        gatewayClient.post(CORE_SERVICE + "/members/" + memberId + "/departments", request);
    }

}
