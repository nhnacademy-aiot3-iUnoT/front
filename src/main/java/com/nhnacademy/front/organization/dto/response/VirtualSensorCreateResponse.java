package com.nhnacademy.front.organization.dto.response;


public record VirtualSensorCreateResponse(
        String deviceEui

) {
    public static VirtualSensorCreateResponse from(String deviceEui) {
        return new VirtualSensorCreateResponse(deviceEui);
    }
}