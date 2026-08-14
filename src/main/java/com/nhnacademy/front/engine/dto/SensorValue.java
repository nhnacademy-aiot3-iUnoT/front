package com.nhnacademy.front.engine.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record SensorValue(

        @NotNull(message = "생성 모드는 필수입니다.")
        GenerationMode mode,

        Double min,

        Double max,

        Double fixedValue,

        Double probability

) {

    @AssertTrue(message = "생성 모드에 맞는 값을 입력해야 합니다.")
    public boolean isConfigurationValid() {
        if (mode == null) {
            return true;
        }

        return switch (mode) {
            case RANGE -> isFinite(min) && isFinite(max) && min <= max;
            case FIXED -> isFinite(fixedValue);
            case PROBABILITY -> isFinite(probability)
                    && probability >= 0.0
                    && probability <= 1.0;
        };
    }

    private static boolean isFinite(Double value) {
        return value != null && Double.isFinite(value);
    }
}
