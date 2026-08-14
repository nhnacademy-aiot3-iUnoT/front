package com.nhnacademy.front.engine.dto.response;

import com.nhnacademy.front.engine.dto.VirtualSensorStatus;
import com.nhnacademy.front.engine.dto.VirtualSensorValues;

public record VirtualSensorInfoResponse(

        String deviceEui,

        Long measurementIntervalSeconds,

        VirtualSensorValues virtualSensorValues,

        VirtualSensorStatus status
) {

    public boolean isActive() {
        return status == VirtualSensorStatus.ACTIVE;
    }
}
