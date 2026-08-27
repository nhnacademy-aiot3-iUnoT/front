package com.nhnacademy.front.engine.dto.response;

import com.nhnacademy.front.engine.dto.VirtualSensorStatus;
import com.nhnacademy.front.engine.dto.VirtualSensorValues;

public record VirtualSensorInfoResponse(

        // 아직 가상 센서를 만들지 않은 구역이면 false로 온다. (오류가 아니다)
        boolean registered,

        String deviceEui,

        Long measurementIntervalSeconds,

        VirtualSensorValues virtualSensorValues,

        VirtualSensorStatus status,

        // 구역 센서로 등록되기 전에는 null이다. 이때 만들어진 데이터는 갈 곳이 없어 버려진다.
        Long zoneId
) {

    public boolean isActive() {
        return status == VirtualSensorStatus.ACTIVE;
    }

    public boolean isRegisteredToZone() {
        return zoneId != null;
    }
}
