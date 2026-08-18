package com.nhnacademy.front.organization.dto.response;

import java.math.BigDecimal;

public record ZoneThresholdDetailResponse(
        Long zoneThresholdId,
        Long storageId,
        Long zoneId,
        Long sensorTypeId,
        String organizationName,
        String storageName,
        String zoneName,
        String sensorTypeName,
        String sensorTypeDescription,
        BigDecimal minValue,
        BigDecimal maxValue,
        Integer alertDuration
) {
    public String sensorTypeDescription() {
        return sensorTypeDescription == null || sensorTypeDescription.isBlank()
                ? ""
                : sensorTypeDescription;
    }
}
