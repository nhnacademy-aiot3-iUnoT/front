package com.nhnacademy.front.report.dto;

import java.time.LocalDate;
import java.util.List;

public record ReportDoorResponse(
        Long zoneId,
        long totalOpenCount,
        long totalOpenMinutes,
        int measuredDays,
        List<DailyPointResponse> dailyPoints
) {

    public record DailyPointResponse(
            LocalDate date,
            long openCount,
            long openMinutes
    ) {}
}
