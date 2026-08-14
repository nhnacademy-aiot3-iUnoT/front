package com.nhnacademy.front.engine.dto.response;

import com.nhnacademy.front.engine.dto.SensorValueRange;
import com.nhnacademy.front.engine.dto.VirtualSensorStatus;

public record VirtualSensorInfoResponse(

        String deviceEui,

        Long measurementIntervalSeconds,

        SensorValueRange temperature,

        SensorValueRange humidity,

        SensorValueRange illumination,

        Double doorOpenProbability,

        VirtualSensorStatus status
) {

    public boolean isActive() {
        return status == VirtualSensorStatus.ACTIVE;
    }
}
