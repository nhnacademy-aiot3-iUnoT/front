package com.nhnacademy.front.organization.dto.response;

import com.nhnacademy.front.organization.dto.StorageStatus;

import java.time.LocalDateTime;

public record StorageDetailResponse(
        Long storageId,
        Long organizationId,
        String organizationName,
        String name,
        String description,
        StorageStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public String description() {
        return description == null || description.isBlank()
                ? ""
                : description;
    }
}
