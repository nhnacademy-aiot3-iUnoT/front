package com.nhnacademy.front.inventory.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {

    INBOUND("입고", "green"),
    OUTBOUND("출고", "blue"),
    DISPOSAL("폐기", "red"),
    TRANSFER_OUT("이동 출고", "orange"),
    TRANSFER_IN("이동 입고", "teal"),
    INFO_CORRECTION_OUT("정보정정 출고", "secondary"),
    INFO_CORRECTION_IN("정보정정 입고", "secondary");

    private final String description;
    private final String color;
}
