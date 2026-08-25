package com.nhnacademy.front.organization.dto.response;

import com.nhnacademy.front.organization.dto.StorageStatus;

public record StorageDepartmentResponse(
        Long storageId,
        String name,
        StorageStatus storageStatus
){}
