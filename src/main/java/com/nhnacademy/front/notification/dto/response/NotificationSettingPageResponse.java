package com.nhnacademy.front.notification.dto.response;

import java.util.List;

/**
 * 알림 설정 화면에 담을 정보.
 * 저장소는 여러 부서에 걸칠 수 있어 중복을 제거하고, 어느 부서 소속인지는 라벨로 보여준다.
 */
public record NotificationSettingPageResponse(
        String telegramRecipient,
        boolean telegramEnabled,
        boolean admin,
        List<StorageScope> storages
) {
    public record StorageScope(
            Long storageId,
            String storageName,
            String departmentNames,
            boolean enabled,
            List<ZoneScope> zones,
            List<Long> explicitZoneIds
    ) {
    }

    /**
     * @param enabled   실제로 적용되는 값. 직접 설정이 없으면 저장소 설정을 따른다.
     * @param inherited 직접 설정 없이 저장소 설정을 따르고 있는지
     */
    public record ZoneScope(
            Long zoneId,
            String zoneName,
            boolean enabled,
            boolean inherited
    ) {
    }
}
