package com.nhnacademy.front.engine.controller;

import com.nhnacademy.front.global.error.ApiException;
import com.nhnacademy.front.global.error.ErrorCode;
import com.nhnacademy.front.engine.client.RuleEngineApiClient;
import com.nhnacademy.front.engine.dto.request.VirtualSensorCreateRequest;
import com.nhnacademy.front.engine.dto.request.VirtualSensorStatusRequest;
import com.nhnacademy.front.engine.dto.request.VirtualSensorUpdateRequest;
import com.nhnacademy.front.engine.dto.response.SensorHistoryResponse;
import com.nhnacademy.front.engine.dto.response.SensorLatestResponse;
import com.nhnacademy.front.engine.dto.response.VirtualSensorInfoResponse;
import com.nhnacademy.front.organization.client.OrganizationApiClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/*
    조직 ID는 URL로 받지 않고 로그인한 사용자의 조직을 서버에서 해석한다.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/organizations")
public class RuleEngineController {

    private static final String VIRTUAL_SENSOR_FORM_VIEW = "organization/virtualsensor-create";
    private static final String VIRTUAL_SENSOR_INFO_VIEW = "organization/virtualsensor-info";
    private static final String SENSOR_INFO_VIEW = "organization/sensorInfo";

    private final RuleEngineApiClient ruleEngineApiClient;
    private final OrganizationApiClient orgApiClient;

    /*
        가상 센서데이터 생성 화면
     */
    @GetMapping("/me/storages/{storageId}/zones/{zoneId}/virtual-sensors/create")
    public String showCreateVirtualSensorForm(
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId,
            Model model
    ) {
        addZoneAttributes(model, storageId, zoneId);
        model.addAttribute("edit", false);

        return VIRTUAL_SENSOR_FORM_VIEW;
    }

    /*
        가상 센서데이터 수정 화면
     */
    @GetMapping("/me/storages/{storageId}/zones/{zoneId}/virtual-sensors/edit")
    public String showUpdateVirtualSensorForm(
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId,
            Model model
    ) {
        addZoneAttributes(model, storageId, zoneId);
        model.addAttribute("edit", true); // 수정모드

        VirtualSensorInfoResponse virtualSensor =
                ruleEngineApiClient.getVirtualSensorData(currentOrganizationId(), storageId, zoneId);

        model.addAttribute("virtualSensor", virtualSensor);

        return VIRTUAL_SENSOR_FORM_VIEW;
    }

    /*
        가상 센서데이터 생성
     */
    @PostMapping("/me/storages/{storageId}/zones/{zoneId}/virtual-sensors")
    public String createVirtualSensor(
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId,
            @Valid @ModelAttribute VirtualSensorCreateRequest request,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            addZoneAttributes(model, storageId, zoneId);
            model.addAttribute("edit", false); // 생성모드

            return VIRTUAL_SENSOR_FORM_VIEW;
        }

        ruleEngineApiClient.createVirtualSensorData(
                currentOrganizationId(), storageId, zoneId, request
        );

        return redirectToVirtualSensorInfo(storageId, zoneId);
    }

    /*
        가상 센서 데이터 업데이트
     */
    @PutMapping("/me/storages/{storageId}/zones/{zoneId}/virtual-sensors")
    public String updateVirtualSensor(
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId,
            @Valid @ModelAttribute VirtualSensorUpdateRequest request,
            BindingResult result,
            Model model
    ) {
        Long organizationId = currentOrganizationId();

        if (result.hasErrors()) {
            addZoneAttributes(model, storageId, zoneId);
            model.addAttribute("edit", true); // 수정모드
            model.addAttribute(
                    "virtualSensor",
                    ruleEngineApiClient.getVirtualSensorData(organizationId, storageId, zoneId)
            );

            return VIRTUAL_SENSOR_FORM_VIEW;
        }

        ruleEngineApiClient.updateVirtualSensorData(organizationId, storageId, zoneId, request);

        return redirectToVirtualSensorInfo(storageId, zoneId);
    }

    /*
        가상 센서 삭제
     */
    @DeleteMapping("/me/storages/{storageId}/zones/{zoneId}/virtual-sensors")
    public String deleteVirtualSensor(
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId
    ) {
        ruleEngineApiClient.deleteVirtualSensorData(currentOrganizationId(), storageId, zoneId);

        return "redirect:" + zoneBasePath(storageId, zoneId) + "/sensorInfo";
    }

    /*
        가상 센서데이터 설정 정보 화면
     */
    @GetMapping("/me/storages/{storageId}/zones/{zoneId}/virtual-sensors")
    public String virtualSensorInfo(
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId,
            Model model
    ) {
        addZoneAttributes(model, storageId, zoneId);

        try {
            VirtualSensorInfoResponse virtualSensor =
                    ruleEngineApiClient.getVirtualSensorData(currentOrganizationId(), storageId, zoneId);

            model.addAttribute("virtualSensor", virtualSensor);

        } catch (ApiException e) {
            // 아직 가상 센서를 만들지 않은 Zone도 화면은 열리도록 한다.
            // 설정이 없는 경우(R009)는 오류가 아니라 '미등록' 상태로 안내한다.
            if (e.getErrorCode() != ErrorCode.R009) {
                model.addAttribute("virtualSensorErrorMessage", e.getMessage());
            }
        }

        return VIRTUAL_SENSOR_INFO_VIEW;
    }

    /*
        가상 센서 활성화 / 비활성화
     */
    @PutMapping("/me/storages/{storageId}/zones/{zoneId}/virtual-sensors/status")
    public String changeVirtualSensorStatus(
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId,
            @Valid @ModelAttribute VirtualSensorStatusRequest request,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return redirectToVirtualSensorInfo(storageId, zoneId);
        }

        ruleEngineApiClient.changeVirtualSensorStatus(
                currentOrganizationId(), storageId, zoneId, request
        );

        return redirectToVirtualSensorInfo(storageId, zoneId);
    }

    /*
        zone의 센서의 상세 데이터 정보 화면
        (센서 데이터 조회는 zoneId만 필요하므로 조직 정보를 따로 조회하지 않는다)
     */
    @GetMapping("/me/storages/{storageId}/zones/{zoneId}/sensorInfo")
    public String info(
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId,
            Model model
    ) {
        addZoneAttributes(model, storageId, zoneId);

        try {
            List<SensorLatestResponse> latestSensors =
                    ruleEngineApiClient.getLatestSensors(zoneId);

            model.addAttribute("latestSensors", latestSensors);

        } catch (ApiException e) {
            model.addAttribute("latestSensors", List.of());
            model.addAttribute("latestSensorsErrorMessage", e.getMessage());
        }


        try {
            List<SensorHistoryResponse> sensorHistory =
                    ruleEngineApiClient.getSensorHistory(zoneId);

            model.addAttribute("sensorHistory", sensorHistory);

        } catch (ApiException e) {
            model.addAttribute("sensorHistory", List.of());
            model.addAttribute("sensorHistoryErrorMessage", e.getMessage());
        }

        return SENSOR_INFO_VIEW;
    }

    /*
        로그인한 사용자가 속한 조직의 ID
     */
    private Long currentOrganizationId() {
        return orgApiClient.getOrgInfo().id();
    }

    private void addZoneAttributes(
            Model model,
            Long storageId,
            Long zoneId
    ) {
        model.addAttribute("storageId", storageId);
        model.addAttribute("zoneId", zoneId);
    }

    private String redirectToVirtualSensorInfo(
            Long storageId,
            Long zoneId
    ) {
        return "redirect:" + zoneBasePath(storageId, zoneId) + "/virtual-sensors";
    }

    private String zoneBasePath(
            Long storageId,
            Long zoneId
    ) {
        return "/organizations/me"
                + "/storages/" + storageId
                + "/zones/" + zoneId;
    }
}
