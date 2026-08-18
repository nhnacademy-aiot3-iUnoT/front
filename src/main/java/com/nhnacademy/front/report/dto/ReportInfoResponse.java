package com.nhnacademy.front.report.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReportInfoResponse(
        Long reportId,
        Long organizationId,
        ReportType reportType,
        LocalDate periodStart,
        LocalDate periodEnd,
        String aiSummary,
        LocalDateTime createdAt,
        List<ReportItemResponse> items
) {}
