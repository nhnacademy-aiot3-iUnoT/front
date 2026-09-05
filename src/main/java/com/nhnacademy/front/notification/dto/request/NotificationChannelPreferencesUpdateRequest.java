package com.nhnacademy.front.notification.dto.request;

import com.nhnacademy.front.notification.dto.NotificationChannel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NotificationChannelPreferencesUpdateRequest(
        @NotNull(message = "알림 채널 정보가 필요합니다.")
        List<@Valid NotificationChannelPreferenceUpdateItem> channels
) {
    public record NotificationChannelPreferenceUpdateItem(
            @NotNull(message = "알림 채널을 선택해주세요.")
            NotificationChannel channel,
            String recipient,
            boolean enabled
    ) {
    }
}
