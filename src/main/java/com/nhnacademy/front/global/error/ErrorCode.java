package com.nhnacademy.front.global.error;

public enum ErrorCode {

    // Account
    A009,

    // Inventory
    S001,

    // Forbidden
    G002,

    UNKNOWN;

    public static ErrorCode from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
