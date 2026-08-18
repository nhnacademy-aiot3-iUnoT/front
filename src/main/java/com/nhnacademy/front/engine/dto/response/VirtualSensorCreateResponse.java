package com.nhnacademy.front.engine.dto.response;


public record VirtualSensorCreateResponse(
        String deviceEui

) {
    public static VirtualSensorCreateResponse from(String deviceEui) {
        return new VirtualSensorCreateResponse(deviceEui);
    }
}