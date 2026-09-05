package com.nhnacademy.front.notification.controller;

import com.nhnacademy.front.global.error.ApiException;
import com.nhnacademy.front.notification.client.NotificationPreferenceApiClient;
import com.nhnacademy.front.notification.dto.NotificationChannel;
import com.nhnacademy.front.notification.dto.request.NotificationChannelPreferencesUpdateRequest;
import com.nhnacademy.front.notification.dto.request.NotificationScopePreferenceRequest;
import com.nhnacademy.front.notification.dto.response.NotificationSettingPageResponse;
import com.nhnacademy.front.notification.service.NotificationSettingPageService;
import com.nhnacademy.front.organization.client.OrganizationApiClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/notifications")
public class NotificationSettingController {

    private final NotificationSettingPageService notificationSettingPageService;
    private final NotificationPreferenceApiClient preferenceApiClient;
    private final OrganizationApiClient organizationApiClient;

    @GetMapping
    public String notificationSetting(Model model) {
        Long organizationId = organizationApiClient.getOrgInfo().id();
        NotificationSettingPageResponse page = notificationSettingPageService.getPage(organizationId);

        model.addAttribute("notificationSetting", page);

        return "notification/notification-setting";
    }

    /**
     * 내 텔레그램 수신 설정 저장
     */
    @PutMapping("/channels")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateChannel(
            @RequestBody @Valid ChannelUpdateForm form
    ) {
        Long organizationId = organizationApiClient.getOrgInfo().id();

        preferenceApiClient.updateChannels(organizationId, new NotificationChannelPreferencesUpdateRequest(
                List.of(new NotificationChannelPreferencesUpdateRequest.NotificationChannelPreferenceUpdateItem(
                        NotificationChannel.TELEGRAM,
                        form.recipient(),
                        form.enabled()
                ))
        ));

        return ResponseEntity.noContent().build();
    }

    /**
     * 저장소 또는 구역 알림 켜고 끄기
     */
    @PutMapping("/scopes")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateScope(
            @RequestBody NotificationScopePreferenceRequest request
    ) {
        Long organizationId = organizationApiClient.getOrgInfo().id();

        preferenceApiClient.upsertScope(organizationId, request);

        return ResponseEntity.noContent().build();
    }

    /**
     * 저장소 알림을 켜고 끈다. 그 저장소에 속한 구역도 전부 같은 값이 되도록 개별 설정을 지운다.
     */
    @PutMapping("/scopes/storages/{storage-id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateStorageScope(
            @PathVariable("storage-id") Long storageId,
            @RequestBody StorageScopeUpdateForm form
    ) {
        Long organizationId = organizationApiClient.getOrgInfo().id();

        preferenceApiClient.upsertScope(organizationId,
                new NotificationScopePreferenceRequest(storageId, null, form.enabled()));

        // 구역별로 따로 잡아둔 설정을 지워야 저장소 설정이 그대로 적용된다.
        for (Long zoneId : form.zoneIds()) {
            preferenceApiClient.deleteScope(organizationId, storageId, zoneId);
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * fetch로 호출되므로 전역 에러 화면(HTML) 대신 JSON으로 내려준다.
     */
    @ExceptionHandler(ApiException.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleApiException(ApiException e) {
        String message = e.getMessage() != null ? e.getMessage() : "요청 처리에 실패했습니다.";

        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    public record ChannelUpdateForm(String recipient, boolean enabled) {
    }

    /**
     * @param zoneIds 직접 설정이 남아 있어 지워야 하는 구역
     */
    public record StorageScopeUpdateForm(boolean enabled, List<Long> zoneIds) {
    }
}
