package com.nhnacademy.front.report.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReportInfoResponse(
        Long reportId,
        Long organizationId,
        Long storageId,
        ReportType reportType,
        LocalDate periodStart,
        LocalDate periodEnd,
        String aiSummary,
        AiSummaryStatus aiSummaryStatus,
        LocalDateTime createdAt,
        List<ReportItemResponse> items,
        List<ReportEnvironmentResponse> environments,
        List<ReportDoorResponse> doors
) {

    public boolean hasEnvironment() {
        return (environments != null && !environments.isEmpty())
                || (doors != null && !doors.isEmpty());
    }
}
