package com.nhnacademy.front.notification.dto.response;

import com.nhnacademy.front.notification.dto.NotificationChannel;

public record NotificationChannelPreferenceResponse(
        NotificationChannel channel,
        String recipient,
        boolean enabled
) {
}
