package com.nhnacademy.front.organization.dto.response;

import com.nhnacademy.front.organization.dto.EnvStatus;
import com.nhnacademy.front.organization.dto.ZoneStatus;

import java.time.LocalDateTime;

public record ZoneInfoResponse(

        Long zoneId,
        Long storageId,
        String name,
        ZoneStatus status,
        EnvStatus envStatus
) {
}
