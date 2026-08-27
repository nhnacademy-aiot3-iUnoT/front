package com.nhnacademy.front.engine.dto.response;

import com.nhnacademy.front.engine.dto.VirtualSensorStatus;
import com.nhnacademy.front.engine.dto.VirtualSensorValues;

public record VirtualSensorInfoResponse(

        // 아직 가상 센서를 만들지 않은 구역이면 false로 온다. (오류가 아니다)
        boolean registered,

        String deviceEui,

        Long measurementIntervalSeconds,

        VirtualSensorValues virtualSensorValues,

        VirtualSensorStatus status
) {

    public boolean isActive() {
        return status == VirtualSensorStatus.ACTIVE;
    }
}
