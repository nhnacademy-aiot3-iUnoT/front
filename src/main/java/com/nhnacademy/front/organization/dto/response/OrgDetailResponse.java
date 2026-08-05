package com.nhnacademy.front.organization.dto.response;

import com.nhnacademy.front.admin.dto.OrganizationStatus;

import java.time.LocalDateTime;

public record OrgDetailResponse(
        Long id,
        String name,
        String roadAddress,
        String zipCode,
        String addressDetail,
        String description,
        OrganizationStatus status,
        LocalDateTime createdAt
) {
    public String description() {
        return description == null || description.isBlank()
                ? ""
                : description;
    }
}
