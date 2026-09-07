package com.nhnacademy.front.notification.dto.response;

public record NotificationScopePreferenceResponse(
        Long storageId,
        Long zoneId,
        boolean enabled
) {
}
