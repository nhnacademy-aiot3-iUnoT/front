package com.nhnacademy.front.organization.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InvitationStatus {
    ACTIVE("진행중"),
    USED("사용"),
    CANCELED("취소"),
    REISSUED("재발급");

    private final String ko;
}
