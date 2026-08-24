package com.nhnacademy.front.engine.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record VirtualSensorValues(
        @NotEmpty(message = "센서 타입을 하나 이상 선택해야 합니다.")
        Map<@NotNull SensorType, @NotNull @Valid SensorValue> valueMap
) {

    public SensorValue temperature() {
        return get(SensorType.TEMPERATURE);
    }

    public SensorValue humidity() {
        return get(SensorType.HUMIDITY);
    }

    public SensorValue illumination() {
        return get(SensorType.ILLUMINATION);
    }

    public SensorValue door() {
        return get(SensorType.DOOR);
    }

    @AssertTrue(message = "센서 타입에 맞는 생성 모드를 선택해야 합니다.")
    public boolean isSensorModeConfigurationValid() {
        if (valueMap == null) {
            return true;
        }

        return valueMap.entrySet().stream().allMatch(entry -> {
            SensorType sensorType = entry.getKey();
            SensorValue sensorValue = entry.getValue();

            if (sensorType == null || sensorValue == null || sensorValue.mode() == null) {
                return true;
            }

            if (sensorType != SensorType.DOOR) {
                return sensorValue.mode() != GenerationMode.PROBABILITY;
            }

            if (sensorValue.mode() == GenerationMode.PROBABILITY) {
                return true;
            }

            return sensorValue.mode() == GenerationMode.FIXED
                    && sensorValue.fixedValue() != null
                    && (Double.compare(sensorValue.fixedValue(), 0.0) == 0
                        || Double.compare(sensorValue.fixedValue(), 1.0) == 0);
        });
    }

    private SensorValue get(SensorType sensorType) {
        return valueMap == null ? null : valueMap.get(sensorType);
    }
}
