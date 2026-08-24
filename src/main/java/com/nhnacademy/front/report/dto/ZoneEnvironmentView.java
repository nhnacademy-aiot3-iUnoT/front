package com.nhnacademy.front.report.dto;

import java.util.List;

public record ZoneEnvironmentView(
        Long zoneId,
        String zoneName,
        List<ReportEnvironmentResponse> sensors,
        ReportDoorResponse door
) {

    public boolean hasDoor() {
        return door != null;
    }

    public boolean hasOutOfRange() {
        return sensors.stream().anyMatch(ReportEnvironmentResponse::hasOutOfRange);
    }

    public int outOfRangeSensorCount() {
        return (int) sensors.stream().filter(ReportEnvironmentResponse::hasOutOfRange).count();
    }
}
