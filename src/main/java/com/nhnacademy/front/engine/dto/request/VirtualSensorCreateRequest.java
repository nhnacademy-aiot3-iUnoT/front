package com.nhnacademy.front.engine.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record VirtualSensorCreateRequest(

        @NotBlank(message = "deviceEui는 필수입니다.")
        String deviceEui,

        @NotNull(message = "측정 주기는 필수입니다.")
        @Min(value = 1, message = "측정 주기는 1초 이상이어야 합니다.")
        Integer measurementIntervalSeconds,

        @Valid
        @NotNull(message = "온도 범위는 필수입니다.")
        SensorValueRange temperature,

        @Valid
        @NotNull(message = "습도 범위는 필수입니다.")
        SensorValueRange humidity,

        @Valid
        @NotNull(message = "밝기 범위는 필수입니다.")
        SensorValueRange illumination,

        @NotNull(message = "문 열림 확률은 필수입니다.")
        @DecimalMin(value = "0.0", message = "문 열림 확률은 0 이상이어야 합니다.")
        @DecimalMax(value = "1.0", message = "문 열림 확률은 1 이하여야 합니다.")
        Double doorOpenProbability
) {

    public record SensorValueRange(

            @NotNull(message = "최솟값은 필수입니다.")
            Double min,

            @NotNull(message = "최댓값은 필수입니다.")
            Double max

    ) {
        public SensorValueRange {
            if (min != null && max != null && min > max) {
                throw new IllegalArgumentException(
                        "최솟값은 최댓값보다 클 수 없습니다."
                );
            }
        }
    }

}