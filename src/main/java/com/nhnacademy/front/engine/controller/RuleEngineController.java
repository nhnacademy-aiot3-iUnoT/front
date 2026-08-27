package com.nhnacademy.front.engine.controller;

import com.nhnacademy.front.global.error.ApiException;
import com.nhnacademy.front.global.error.ErrorCode;
import com.nhnacademy.front.engine.client.RuleEngineApiClient;
import com.nhnacademy.front.engine.dto.response.SensorHistoryResponse;
import com.nhnacademy.front.engine.dto.response.StorageZonesResponse;
import com.nhnacademy.front.engine.dto.response.SensorLatestResponse;
import com.nhnacademy.front.organization.client.OrganizationApiClient;
import com.nhnacademy.front.organization.client.StorageApiClient;
import com.nhnacademy.front.organization.client.ZoneApiClient;
import com.nhnacademy.front.organization.dto.response.StorageInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/*
    조직 ID는 URL로 받지 않고 로그인한 사용자의 조직을 서버에서 해석한다.
 */
@Controller
@RequiredArgsConstructor
public class RuleEngineController {

    private static final String SENSOR_INFO_VIEW = "organization/sensorInfo";
    private static final String ENVIRONMENT_MONITORING_VIEW = "organization/environment-monitoring";

    private final RuleEngineApiClient ruleEngineApiClient;
    private final StorageApiClient storageApiClient;
    private final ZoneApiClient zoneApiClient;
    private final OrganizationApiClient organizationApiClient;

    /*
        환경 관리 화면
        내 조직의 저장소 목록과 저장소별 구역 목록을 함께 보여준다.
        구역을 고르면 그 구역의 센서 상세 화면으로 넘어간다.
     */
    @GetMapping("/environmentMonitoring")
    public String environmentMonitoring(
            Model model
    ) {
        List<StorageInfoResponse> storages = storageApiClient.getStorages();

        List<StorageZonesResponse> storageZones = storages.stream()
                .map(storage -> new StorageZonesResponse(
                        storage.storageId(),
                        storage.name(),
                        storage.status(),
                        zoneApiClient.getZones(storage.storageId())
                ))
                .toList();

        model.addAttribute("storageZones", storageZones);
        model.addAttribute(
                "organizationName",
                storages.isEmpty() ? null : storages.getFirst().organizationName()
        );

        return ENVIRONMENT_MONITORING_VIEW;
    }

    /*
        zone의 센서의 상세 데이터 정보 화면
     */
    @GetMapping("/organizations/me/storages/{storage-id}/zones/{zone-id}/sensorInfo")
    public String info(
            @PathVariable("storage-id") Long storageId,
            @PathVariable("zone-id") Long zoneId,
            Model model
    ) {
        addZoneAttributes(model, storageId, zoneId);

        Long organizationId = myOrganizationId();

        try {
            List<SensorLatestResponse> latestSensors =
                    ruleEngineApiClient.getLatestSensors(organizationId, zoneId);

            model.addAttribute("latestSensors", latestSensors);

        } catch (ApiException e) {
            model.addAttribute("latestSensors", List.of());
            model.addAttribute("latestSensorsErrorMessage", queryFailureMessage(e));
        }


        try {
            List<SensorHistoryResponse> sensorHistory =
                    ruleEngineApiClient.getSensorHistory(organizationId, zoneId);

            model.addAttribute("sensorHistory", sensorHistory);

        } catch (ApiException e) {
            model.addAttribute("sensorHistory", List.of());
            model.addAttribute("sensorHistoryErrorMessage", queryFailureMessage(e));
        }

        return SENSOR_INFO_VIEW;
    }

    private String queryFailureMessage(ApiException e) {
        if (e.getErrorCode() != ErrorCode.S001) {
            throw e;
        }

        return e.getMessage();
    }

    /*
        룰엔진은 가상 센서 URL에 조직 ID를 요구하므로 로그인한 사용자의 조직을 조회해서 채운다.
     */
    private Long myOrganizationId() {
        return organizationApiClient.getOrgInfo().id();
    }

    private void addZoneAttributes(
            Model model,
            Long storageId,
            Long zoneId
    ) {
        model.addAttribute("storageId", storageId);
        model.addAttribute("zoneId", zoneId);
    }
}
