package com.nhnacademy.front.engine.dto.request;

import com.nhnacademy.front.engine.dto.VirtualSensorValues;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VirtualSensorCreateRequest(

        @NotBlank(message = "deviceEui는 필수입니다.")
        String deviceEui,

        @NotNull(message = "측정 주기는 필수입니다.")
        @Min(value = 1, message = "측정 주기는 1초 이상이어야 합니다.")
        Long measurementIntervalSeconds,

        @NotNull(message = "센서 타입을 하나 이상 선택해야 합니다.")
        @Valid
        VirtualSensorValues virtualSensorValues
) {
}
