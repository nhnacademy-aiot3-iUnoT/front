package com.nhnacademy.front.report.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {
    WEEKLY("주간"),
    MONTHLY("월간");

    private final String name;
}
