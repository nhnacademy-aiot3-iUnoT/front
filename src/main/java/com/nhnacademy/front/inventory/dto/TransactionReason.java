package com.nhnacademy.front.inventory.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum TransactionReason {

    DISPENSING("조제·처방 출고"),
    STORAGE_TRANSFER("저장소 이동"),
    RETURN_TO_SUPPLIER("공급처 반품"),
    EXPIRED("유통기한 만료"),
    DETERIORATED("변질"),
    OTHER("기타");

    private final String description;

    // 알 수 없는 값은 그대로 보여줌. 직접 입력한 사유가 들어올 수 있음
    public static String describe(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(value -> value.name().equals(reason))
                .map(TransactionReason::getDescription)
                .findFirst()
                .orElse(reason);
    }
}
