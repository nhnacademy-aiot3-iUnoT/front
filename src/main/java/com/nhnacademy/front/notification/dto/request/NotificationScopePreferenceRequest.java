package com.nhnacademy.front.notification.dto.request;

public record NotificationScopePreferenceRequest(
        Long storageId,
        Long zoneId,
        Boolean enabled
) {
}
