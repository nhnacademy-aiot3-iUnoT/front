package com.nhnacademy.front.organization.dto.response;

public record ZoneSensorInfoResponse(
        Long zoneSensorId,
        Long zoneId,
        String deviceEui,
        String name
) {
}
