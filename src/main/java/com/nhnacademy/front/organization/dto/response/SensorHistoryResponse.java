package com.nhnacademy.front.organization.dto.response;

import java.time.Instant;

public record SensorHistoryResponse(
        String sensorType,
        String unit,
        Instant time,
        Double value
) {
}
