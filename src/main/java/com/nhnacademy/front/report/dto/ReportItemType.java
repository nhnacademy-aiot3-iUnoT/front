package com.nhnacademy.front.report.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportItemType {
    INBOUND("입고"),
    OUTBOUND("출고"),
    DISPOSAL("폐기");

    private final String description;
}
