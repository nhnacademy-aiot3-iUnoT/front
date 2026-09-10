package com.nhnacademy.front.inventory.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EnvironmentEventItemResponse(
        Long environmentEventId,
        BigDecimal detectedValue,
        BigDecimal thresholdValue,
        String environmentType,
        String breachType,
        LocalDateTime createdAt
) {
}
