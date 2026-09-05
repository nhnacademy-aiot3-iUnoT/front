package com.nhnacademy.front.global.error;

// 담당자가 code 추가 (message x) -> API Server에서 만든거 가져오기
public enum ErrorCode {

    // Account
    A009,

    // Organization
    O001,

    // Inventory
    S001,

    // Medicine

    UNKNOWN;

    public static ErrorCode from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
