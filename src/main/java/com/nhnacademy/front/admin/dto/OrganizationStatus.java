package com.nhnacademy.front.admin.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrganizationStatus {
    PENDING("진행중"),
    ACTIVE("활성"),
    INACTIVE("비활성"),
    SUSPENDED("종료");

    private final String ko;
}
