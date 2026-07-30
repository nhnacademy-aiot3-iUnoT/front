package com.nhnacademy.front.organization.dto.response;

public record SensorLatestResponse(
        Long organizationId,
        String deviceEui,
        Long storageId,
        Long zoneId,
        String sensorType,
        Double value,
        String unit,
        String measuredAt
) {

}