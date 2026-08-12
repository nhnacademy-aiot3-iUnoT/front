package com.nhnacademy.front.engine.dto.response;

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