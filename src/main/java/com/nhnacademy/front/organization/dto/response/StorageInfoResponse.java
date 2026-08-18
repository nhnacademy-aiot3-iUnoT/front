package com.nhnacademy.front.organization.dto.response;

import com.nhnacademy.front.organization.dto.StorageStatus;

import java.time.LocalDateTime;

public record StorageInfoResponse(
        Long storageId,
        Long organizationId,
        String organizationName,
        String name,
        StorageStatus status
) {
}
