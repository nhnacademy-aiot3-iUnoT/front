package com.nhnacademy.front.organization.dto.request;

import com.nhnacademy.front.organization.dto.VirtualSensorStatus;
import jakarta.validation.constraints.NotNull;

public record VirtualSensorStatusRequest(

        @NotNull(message = "변경하고자 하는 가상 센서 상태를 입력해주세요")
        VirtualSensorStatus status
) {
}
