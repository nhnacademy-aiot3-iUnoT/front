package com.nhnacademy.front.organization.dto.response;

import com.nhnacademy.front.organization.dto.OrganizationRole;

import java.util.List;

// 부서 상세 조회 페이지에 담을 정보
public record DepartmentPageResponse(
        DepartmentInfoResponse department,
        OrganizationRole memberRole,
        List<StorageDepartmentResponse> storages,
        List<OrganizationMemberResponse> members
) {
}
