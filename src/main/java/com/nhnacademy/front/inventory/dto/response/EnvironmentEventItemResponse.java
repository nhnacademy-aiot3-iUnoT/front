package com.nhnacademy.front.inventory.dto.response;

import com.nhnacademy.front.inventory.dto.BreachType;
import com.nhnacademy.front.inventory.dto.EnvironmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EnvironmentEventItemResponse(
        Long environmentEventId,
        BigDecimal detectedValue,
        BigDecimal thresholdValue,
        EnvironmentType environmentType,
        BreachType breachType,
        LocalDateTime createdAt
) {
}
