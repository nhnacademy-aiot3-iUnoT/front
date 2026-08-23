package com.nhnacademy.front.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportEnvironmentResponse(
        Long zoneId,
        String sensorType,
        String unit,
        BigDecimal avgValue,
        BigDecimal minValue,
        BigDecimal maxValue,

        BigDecimal thresholdMin,
        BigDecimal thresholdMax,

        int outOfRangeDays,
        int measuredDays,
        List<DailyPointResponse> dailyPoints
) {

    public record DailyPointResponse(
            LocalDate date,
            BigDecimal avgValue,
            BigDecimal minValue,
            BigDecimal maxValue
    ) {}

    public boolean hasThreshold() {
        return thresholdMin != null || thresholdMax != null;
    }

    public boolean hasOutOfRange() {
        return outOfRangeDays > 0;
    }
}
