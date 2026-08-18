package com.nhnacademy.front.organization.dto.response;

import com.nhnacademy.front.organization.dto.DepartmentStatus;

import java.time.LocalDateTime;

public record DepartmentInfoResponse(
        Long id,
        String name,
        String description,
        DepartmentStatus status,
        LocalDateTime createdAt
) {
}
