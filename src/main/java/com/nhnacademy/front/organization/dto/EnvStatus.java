package com.nhnacademy.front.organization.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnvStatus {

    NORMAL("정상"),
    WARNING("경고"),
    CRITICAL("위험");


    private final String ko;

}
