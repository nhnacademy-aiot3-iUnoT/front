package com.nhnacademy.front.dashboard.dto.response;

import java.util.List;

public record DashboardDepartmentsResponse(
        boolean orgAdmin,
        String organizationName,
        List<DepartmentOptionResponse> myDepartments,
        List<DepartmentOptionResponse> otherDepartments
) {
    public record DepartmentOptionResponse(
            Long departmentId,
            String name
    ) {
    }
}
