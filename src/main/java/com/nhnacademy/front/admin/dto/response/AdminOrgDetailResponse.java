package com.nhnacademy.front.admin.dto.response;

import com.nhnacademy.front.admin.dto.OrganizationStatus;

import java.time.LocalDateTime;

public record AdminOrgDetailResponse(
        Long id,
        String businessNumber,
        String name,
        String roadAddress,
        String zipCode,
        String addressDetail,
        OrganizationStatus status,
        LocalDateTime createdAt
) {
}
