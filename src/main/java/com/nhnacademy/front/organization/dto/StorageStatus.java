package com.nhnacademy.front.organization.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StorageStatus {
    ACTIVE("활성화"),
    INACTIVE("비활성화"),
    CLOSED("삭제");

    private final String ko;
}
