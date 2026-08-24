package com.nhnacademy.front.organization.dto.response;


public record StorageInfoResponse(
        Long storageId,
        Long organizationId,
        String name
) {
}
