package com.nhnacademy.front.organization.dto.response;

import com.nhnacademy.front.organization.dto.EnvStatus;
import com.nhnacademy.front.organization.dto.ZoneStatus;

import java.time.LocalDateTime;

public record ZoneDetailResponse(
        Long zoneId,
        Long storageId,
        String organizationName,
        String storageName,
        String name,
        String description,
        ZoneStatus status,
        EnvStatus envStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public String description() {
        return description == null || description.isBlank()
                ? ""
                : description;
    }
}
