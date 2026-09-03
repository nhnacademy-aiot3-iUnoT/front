package com.nhnacademy.front.dashboard.dto.response;

import java.util.List;

/**
 * 부서 선택 드롭다운.
 * 관리자면 조직의 모든 부서가, 일반 조직원이면 자기 소속 부서만 담긴다.
 */
public record DashboardDepartmentsResponse(
        boolean orgAdmin,
        String organizationName,
        List<DepartmentOptionResponse> departments
) {
    /** 응답이 비었을 때도 템플릿이 그대로 돌도록 빈 목록을 보장한다. */
    public List<DepartmentOptionResponse> options() {
        return departments == null ? List.of() : departments;
    }

    public record DepartmentOptionResponse(
            Long departmentId,
            String name
    ) {
    }
}
