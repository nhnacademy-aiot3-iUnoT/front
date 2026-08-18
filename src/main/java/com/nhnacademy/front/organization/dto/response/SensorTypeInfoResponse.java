package com.nhnacademy.front.organization.dto.response;

public record SensorTypeInfoResponse(
        Long sensorTypeId,
        String name,
        String description
) {
    public String description() {
        return description == null || description.isBlank()
                ? ""
                : description;
    }
}
