package com.nhnacademy.front.engine.dto;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * 룰 엔진에서 사용하는 표준 센서 타입과 단위를 정의한다.
 */
public enum SensorType {
    TEMPERATURE("temperature", "C", "온도"),
    HUMIDITY("humidity", "%", "습도"),
    DOOR("door", "문열림 여부", "문"),
    ILLUMINATION("illumination", "lux", "조도");

    private final String value;
    private final String unit;
    private final String ko;

    SensorType(String value, String unit, String ko) {
        this.value = value;
        this.unit = unit;
        this.ko = ko;
    }

    public String value() {
        return value;
    }

    public String unit() {
        return unit;
    }

    public String ko() {
        return ko;
    }

    public static Optional<SensorType> findByValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String normalizedValue = value.trim().toLowerCase(Locale.ROOT);

        return Arrays.stream(values())
                .filter(sensorType -> sensorType.value.equals(normalizedValue))
                .findFirst();
    }
}
