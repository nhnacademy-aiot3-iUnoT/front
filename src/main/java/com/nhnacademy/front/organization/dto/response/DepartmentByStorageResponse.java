package com.nhnacademy.front.organization.dto.response;

import com.nhnacademy.front.organization.dto.DepartmentStatus;

public record DepartmentByStorageResponse(
        Long departmentId,
        String name,
        DepartmentStatus departmentStatus
) {
}