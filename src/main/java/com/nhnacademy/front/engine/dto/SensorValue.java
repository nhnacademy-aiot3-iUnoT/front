package com.nhnacademy.front.engine.dto;

import jakarta.validation.constraints.NotNull;

public record SensorValue(

        GenerationMode mode,

        @NotNull(message = "최솟값은 필수입니다.")
        Double min,

        @NotNull(message = "최댓값은 필수입니다.")
        Double max,

        Double fixedValue,

        Double probability

) {
    public SensorValue {
        if (min != null && max != null && min > max) {
            throw new IllegalArgumentException(
                    "최솟값은 최댓값보다 클 수 없습니다."
            );
        }
    }
}
