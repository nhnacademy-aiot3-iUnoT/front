package com.nhnacademy.front.engine.dto.request;

import com.nhnacademy.front.engine.dto.SensorType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.Set;

public record VirtualSensorCreateRequest(

        @NotBlank(message = "deviceEui는 필수입니다.")
        String deviceEui,

        @NotNull(message = "측정 주기는 필수입니다.")
        @Min(value = 1, message = "측정 주기는 1초 이상이어야 합니다.")
        Integer measurementIntervalSeconds,

        @NotEmpty(message = "센서 타입을 하나 이상 선택해야 합니다.")
        Set<SensorType> enabledSensorTypes,

        @Valid
        SensorValueRange temperature,

        @Valid
        SensorValueRange humidity,

        @Valid
        SensorValueRange illumination,


        @DecimalMin(value = "0.0", message = "문 열림 확률은 0 이상이어야 합니다.")
        @DecimalMax(value = "1.0", message = "문 열림 확률은 1 이하여야 합니다.")
        Double doorOpenProbability
) {

    @AssertTrue(message = "선택한 센서 타입의 설정값을 입력해야 합니다.")
    public boolean isSelectedSensorConfigurationValid() {
        if (enabledSensorTypes == null || enabledSensorTypes.isEmpty()) {
            return true; // 비어 있는 검증은 @NotEmpty에서 예외 처리
        }

        return (!enabledSensorTypes.contains(SensorType.TEMPERATURE)
                || temperature != null)
                && (!enabledSensorTypes.contains(SensorType.HUMIDITY)
                || humidity != null)
                && (!enabledSensorTypes.contains(SensorType.ILLUMINATION)
                || illumination != null)
                && (!enabledSensorTypes.contains(SensorType.DOOR)
                || doorOpenProbability != null);
    }

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