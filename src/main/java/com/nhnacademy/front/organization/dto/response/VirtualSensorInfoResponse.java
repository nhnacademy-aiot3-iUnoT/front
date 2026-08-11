package com.nhnacademy.front.organization.dto.response;

import com.nhnacademy.front.organization.dto.VirtualSensorStatus;

public record VirtualSensorInfoResponse(

        String deviceEui,

        Long measurementIntervalSeconds,

        SensorValueRange temperature,

        SensorValueRange humidity,

        SensorValueRange illumination,

        Double doorOpenProbability,

        VirtualSensorStatus status
) {

    public record SensorValueRange(
            Double min,
            Double max
    ) {
    }

    public boolean isActive() {
        return status == VirtualSensorStatus.ACTIVE;
    }
}
