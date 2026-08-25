package com.nhnacademy.front.admin.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountStatus {
    ACTIVE("활성"),
    LOCKED("잠김"),
    INACTIVE("비활성"),
    WITHDRAWN("탈퇴");

    private final String ko;
}
