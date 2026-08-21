package com.nhnacademy.front.organization.dto.response;

import com.nhnacademy.front.organization.dto.DepartmentStatus;

public record DepartmentListResponse(
        Long id,
        String name,
        DepartmentStatus status
) {
}
