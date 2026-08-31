package com.nhnacademy.front.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MedicineOutboundRequest(

        @NotNull
        Long medicinePackageUnitId,

        @NotNull(message = "출고 수량을 입력해주세요.")
        @Positive(message = "출고 수량은 1개 이상이어야 합니다.")
        Integer quantity,

        @NotNull
        Long zoneId,

        @NotBlank(message = "출고 사유를 선택해주세요.")
        String reason,

        @Size(max = 100, message = "메모는 100자 이내로 입력해주세요.")
        String memo
) {
}