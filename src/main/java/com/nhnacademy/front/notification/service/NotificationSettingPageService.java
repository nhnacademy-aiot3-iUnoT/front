package com.nhnacademy.front.notification.service;

import com.nhnacademy.front.notification.client.NotificationPreferenceApiClient;
import com.nhnacademy.front.notification.dto.NotificationChannel;
import com.nhnacademy.front.notification.dto.response.NotificationChannelPreferenceResponse;
import com.nhnacademy.front.notification.dto.response.NotificationScopePreferenceResponse;
import com.nhnacademy.front.notification.dto.response.NotificationSettingPageResponse;
import com.nhnacademy.front.organization.client.DepartmentApiClient;
import com.nhnacademy.front.organization.client.OrganizationMemberApiClient;
import com.nhnacademy.front.organization.client.StorageApiClient;
import com.nhnacademy.front.organization.client.StorageDepartmentApiClient;
import com.nhnacademy.front.organization.client.ZoneApiClient;
import com.nhnacademy.front.organization.dto.OrganizationRole;
import com.nhnacademy.front.organization.dto.response.DepartmentListResponse;
import com.nhnacademy.front.organization.dto.response.StorageDepartmentResponse;
import com.nhnacademy.front.organization.dto.response.StorageInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationSettingPageService {

    private final NotificationPreferenceApiClient preferenceApiClient;
    private final DepartmentApiClient departmentApiClient;
    private final StorageDepartmentApiClient storageDepartmentApiClient;
    private final ZoneApiClient zoneApiClient;
    private final StorageApiClient storageApiClient;
    private final OrganizationMemberApiClient memberApiClient;

    public NotificationSettingPageResponse getPage(Long organizationId) {
        // 내가 설정해 둔 알림 범위. 설정이 없으면 알림을 받지 않는다.
        Map<Long, Boolean> storageEnabled = new LinkedHashMap<>();
        Map<Long, Boolean> zoneEnabled = new LinkedHashMap<>();

        for (NotificationScopePreferenceResponse scope : preferenceApiClient.getScopes(organizationId)) {
            if (scope.zoneId() != null) {
                zoneEnabled.put(scope.zoneId(), scope.enabled());
            } else if (scope.storageId() != null) {
                storageEnabled.put(scope.storageId(), scope.enabled());
            }
        }

        // 관리자는 소속 부서와 무관하게 조직 전체 저장소를 설정할 수 있다.
        boolean admin = isAdmin();
        List<NotificationSettingPageResponse.StorageScope> storages = admin
                ? collectOrganizationStorages(storageEnabled, zoneEnabled)
                : collectMyDepartmentStorages(storageEnabled, zoneEnabled);

        NotificationChannelPreferenceResponse telegram = preferenceApiClient.getChannels(organizationId).stream()
                .filter(channel -> channel.channel() == NotificationChannel.TELEGRAM)
                .findFirst()
                .orElse(null);

        return new NotificationSettingPageResponse(
                telegram == null ? "" : telegram.recipient(),
                telegram != null && telegram.enabled(),
                admin,
                storages
        );
    }

    private boolean isAdmin() {
        OrganizationRole role = memberApiClient.getRole().role();

        return role == OrganizationRole.ORG_BOSS || role == OrganizationRole.ORG_OWNER;
    }

    /**
     * 관리자용. 부서에 연결되지 않은 저장소도 포함해야 해서 조직 저장소 목록을 그대로 쓴다.
     */
    private List<NotificationSettingPageResponse.StorageScope> collectOrganizationStorages(
            Map<Long, Boolean> storageEnabled,
            Map<Long, Boolean> zoneEnabled
    ) {
        List<NotificationSettingPageResponse.StorageScope> storages = new ArrayList<>();

        for (StorageInfoResponse storage : storageApiClient.getStorages()) {
            storages.add(toStorageScope(
                    storage.storageId(),
                    storage.name(),
                    null,
                    storageEnabled,
                    zoneEnabled
            ));
        }

        return storages;
    }

    /**
     * 내가 속한 부서의 저장소를 모은다. 한 저장소가 여러 부서에 걸릴 수 있어 중복은 합친다.
     */
    private List<NotificationSettingPageResponse.StorageScope> collectMyDepartmentStorages(
            Map<Long, Boolean> storageEnabled,
            Map<Long, Boolean> zoneEnabled
    ) {
        Map<Long, String> storageNames = new LinkedHashMap<>();
        Map<Long, Set<String>> departmentNames = new LinkedHashMap<>();

        for (DepartmentListResponse department : departmentApiClient.getMyDepartments()) {
            for (StorageDepartmentResponse storage : storageDepartmentApiClient.getStorages(department.id())) {
                storageNames.putIfAbsent(storage.storageId(), storage.name());
                departmentNames
                        .computeIfAbsent(storage.storageId(), id -> new java.util.LinkedHashSet<>())
                        .add(department.name());
            }
        }

        List<NotificationSettingPageResponse.StorageScope> storages = new ArrayList<>();

        for (Map.Entry<Long, String> entry : storageNames.entrySet()) {
            Long storageId = entry.getKey();

            storages.add(toStorageScope(
                    storageId,
                    entry.getValue(),
                    String.join(", ", departmentNames.get(storageId)),
                    storageEnabled,
                    zoneEnabled
            ));
        }

        return storages;
    }

    private NotificationSettingPageResponse.StorageScope toStorageScope(
            Long storageId,
            String storageName,
            String departmentNames,
            Map<Long, Boolean> storageEnabled,
            Map<Long, Boolean> zoneEnabled
    ) {
        boolean storageOn = Boolean.TRUE.equals(storageEnabled.get(storageId));

        List<NotificationSettingPageResponse.ZoneScope> zones = zoneApiClient.getZones(storageId).stream()
                .map(zone -> {
                    // 구역에 직접 설정이 없으면 저장소 설정을 따른다. 룰엔진도 가장 구체적인 설정 하나만 보므로
                    // 설정이 없는 구역은 저장소 설정대로 알림이 간다.
                    Boolean own = zoneEnabled.get(zone.zoneId());
                    boolean effective = own == null ? storageOn : own;

                    return new NotificationSettingPageResponse.ZoneScope(
                            zone.zoneId(),
                            zone.name(),
                            effective,
                            own == null
                    );
                })
                .toList();

        // 저장소를 켜고 끌 때 지워야 할, 구역별로 따로 잡힌 설정들
        List<Long> explicitZoneIds = zones.stream()
                .filter(zone -> !zone.inherited())
                .map(NotificationSettingPageResponse.ZoneScope::zoneId)
                .toList();

        return new NotificationSettingPageResponse.StorageScope(
                storageId,
                storageName,
                departmentNames,
                storageOn,
                zones,
                explicitZoneIds
        );
    }
}
