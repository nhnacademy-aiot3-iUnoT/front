package com.nhnacademy.front.organization.dto.response;

import java.math.BigDecimal;

public record ZoneThresholdInfoResponse(
        Long zoneThresholdId,
        Long zoneId,
        Long sensorTypeId,
        String sensorTypeName,
        BigDecimal minValue,
        BigDecimal maxValue,
        Integer alertDuration
) {
}
