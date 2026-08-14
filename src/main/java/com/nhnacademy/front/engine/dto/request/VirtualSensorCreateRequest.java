package com.nhnacademy.front.engine.dto.request;

import com.nhnacademy.front.engine.dto.SensorType;
import com.nhnacademy.front.engine.dto.SensorValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.Map;
import java.util.Objects;

public record VirtualSensorCreateRequest(

        @NotBlank(message = "deviceEui는 필수입니다.")
        String deviceEui,

        @NotNull(message = "측정 주기는 필수입니다.")
        @Min(value = 1, message = "측정 주기는 1초 이상이어야 합니다.")
        Integer measurementIntervalSeconds,

        @NotEmpty(message = "센서 타입을 하나 이상 선택해야 합니다.")
        Map<@NotNull SensorType,
              @Valid SensorValue
        > virtualSensorValues,


        @DecimalMin(value = "0.0", message = "문 열림 확률은 0 이상이어야 합니다.")
        @DecimalMax(value = "1.0", message = "문 열림 확률은 1 이하여야 합니다.")
        Double doorOpenProbability
) {

    @AssertTrue(message = "선택한 센서 타입의 설정값을 입력해야 합니다.")
    public boolean isSelectedSensorConfigurationValid() {
        if (virtualSensorValues == null || virtualSensorValues.isEmpty()) {
            return true; // 비어 있는 검증은 @NotEmpty에서 예외 처리
        }

        return virtualSensorValues.values().stream().noneMatch(Objects::isNull);
    }
}