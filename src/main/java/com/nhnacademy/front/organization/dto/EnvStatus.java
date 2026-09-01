package com.nhnacademy.front.organization.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnvStatus {

    NORMAL("정상", "green"),
    WARNING("경고", "yellow"),
    CRITICAL("위험", "red");


    private final String ko;

    /** Tabler 색상 이름 (badge bg-{color}-lt, progress-bar bg-{color}) */
    private final String color;
}
