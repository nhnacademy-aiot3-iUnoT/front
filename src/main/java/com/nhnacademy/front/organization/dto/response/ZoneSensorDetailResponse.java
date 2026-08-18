package com.nhnacademy.front.organization.dto.response;

public record ZoneSensorDetailResponse(
        Long zoneSensorId,
        Long storageId,
        Long zoneId,
        String deviceEui,
        String organizationName,
        String storageName,
        String zoneName,
        String name,
        String description
) {
    public String description() {
        return description == null || description.isBlank()
                ? ""
                : description;
    }
}
