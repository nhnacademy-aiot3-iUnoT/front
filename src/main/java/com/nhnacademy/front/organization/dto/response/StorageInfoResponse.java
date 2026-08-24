package com.nhnacademy.front.organization.dto.response;


import com.nhnacademy.front.organization.dto.StorageStatus;

public record StorageInfoResponse(
        Long storageId,
        Long organizationId,
        String organizationName,
        String name,
        StorageStatus status
) {
}
