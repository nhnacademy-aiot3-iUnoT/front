package com.nhnacademy.front.admin.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountStatusAction {
    LOCK("잠금"),
    UNLOCK("잠금 해제"),
    DEACTIVATE("비활성화"),
    REACTIVATE("재활성화");

    private final String ko;
}
