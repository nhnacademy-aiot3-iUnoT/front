package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.global.error.ApiException;
import com.nhnacademy.front.global.error.ErrorCode;
import com.nhnacademy.front.organization.client.RuleEngineApiClient;
import com.nhnacademy.front.organization.dto.request.VirtualSensorCreateRequest;
import com.nhnacademy.front.organization.dto.request.VirtualSensorStatusRequest;
import com.nhnacademy.front.organization.dto.request.VirtualSensorUpdateRequest;
import com.nhnacademy.front.organization.dto.response.SensorHistoryResponse;
import com.nhnacademy.front.organization.dto.response.SensorLatestResponse;
import com.nhnacademy.front.organization.dto.response.VirtualSensorInfoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping(
        "/organizations/{organizationId}" +
                "/storages/{storageId}" +
                "/zones"
)
public class RuleEngineController {

    private static final String VIRTUAL_SENSOR_FORM_VIEW = "organization/virtualsensor-create";
    private static final String VIRTUAL_SENSOR_INFO_VIEW = "organization/virtualsensor-info";

    private final RuleEngineApiClient ruleEngineApiClient;

    /*
        가상 센서데이터 생성 화면
     */
    @GetMapping("/{zoneId}/virtual-sensors/create")
    public String showCreateVirtualSensorForm(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId,
            Model model
    ) {
        addZoneAttributes(model, organizationId, storageId, zoneId);
        model.addAttribute("edit", false);

        return VIRTUAL_SENSOR_FORM_VIEW;
    }

    /*
        가상 센서데이터 수정 화면
     */
    @GetMapping("/{zoneId}/virtual-sensors/edit")
    public String showUpdateVirtualSensorForm(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId,
            Model model
    ) {
        addZoneAttributes(model, organizationId, storageId, zoneId);
        model.addAttribute("edit", true); // 수정모드

        VirtualSensorInfoResponse virtualSensor =
                ruleEngineApiClient.getVirtualSensorData(organizationId, storageId, zoneId);

        model.addAttribute("virtualSensor", virtualSensor);

        return VIRTUAL_SENSOR_FORM_VIEW;
    }

    /*
        가상 센서데이터 생성
     */
    @PostMapping("/{zoneId}/virtual-sensors")
    public String createVirtualSensor(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId,
            @Valid @ModelAttribute VirtualSensorCreateRequest request,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            addZoneAttributes(model, organizationId, storageId, zoneId);
            model.addAttribute("edit", false); // 생성모드

            return VIRTUAL_SENSOR_FORM_VIEW;
        }

        ruleEngineApiClient.createVirtualSensorData(organizationId, storageId, zoneId, request);

        return redirectToVirtualSensorInfo(organizationId, storageId, zoneId);
    }

    /*
        가상 센서 데이터 업데이트
     */
    @PutMapping("/{zoneId}/virtual-sensors")
    public String updateVirtualSensor(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId,
            @Valid @ModelAttribute VirtualSensorUpdateRequest request,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            addZoneAttributes(model, organizationId, storageId, zoneId);
            model.addAttribute("edit", true); // 수정모드
            model.addAttribute(
                    "virtualSensor",
                    ruleEngineApiClient.getVirtualSensorData(organizationId, storageId, zoneId)
            );

            return VIRTUAL_SENSOR_FORM_VIEW;
        }

        ruleEngineApiClient.updateVirtualSensorData(organizationId, storageId, zoneId, request);

        return redirectToVirtualSensorInfo(organizationId, storageId, zoneId);
    }

    /*
        가상 센서 삭제
     */
    @DeleteMapping("/{zoneId}/virtual-sensors")
    public String deleteVirtualSensor(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId
    ) {

        ruleEngineApiClient.deleteVirtualSensorData(organizationId, storageId, zoneId);

        return "redirect:/organizations/" + organizationId + "/storages/" + storageId + "/zones/" + zoneId + "/sensorInfo";
    }

    /*
        가상 센서데이터 설정 정보 화면
     */
    @GetMapping("/{zoneId}/virtual-sensors")
    public String virtualSensorInfo(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId,
            Model model
    ) {
        addZoneAttributes(model, organizationId, storageId, zoneId);

        try {
            VirtualSensorInfoResponse virtualSensor =
                    ruleEngineApiClient.getVirtualSensorData(organizationId, storageId, zoneId);

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
    @PutMapping("/{zoneId}/virtual-sensors/status")
    public String changeVirtualSensorStatus(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId,
            @Valid @ModelAttribute VirtualSensorStatusRequest request,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return redirectToVirtualSensorInfo(organizationId, storageId, zoneId);
        }

        ruleEngineApiClient.changeVirtualSensorStatus(organizationId, storageId, zoneId, request);

        return redirectToVirtualSensorInfo(organizationId, storageId, zoneId);
    }

    /*
        zone의 센서의 상세 데이터 정보 화면
     */
    @GetMapping("/{zoneId}/sensorInfo")
    public String info(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("storageId") Long storageId,
            @PathVariable("zoneId") Long zoneId,
            Model model
    ) {
        model.addAttribute("zoneId", zoneId);
        model.addAttribute("storageId", storageId);
        model.addAttribute("organizationId", organizationId);

        List<SensorLatestResponse> latestSensors = List.of();

        try {
            latestSensors = ruleEngineApiClient.getLatestSensors(zoneId);
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

        return "organization/sensorInfo";
    }

    private void addZoneAttributes(
            Model model,
            Long organizationId,
            Long storageId,
            Long zoneId
    ) {
        model.addAttribute("organizationId", organizationId);
        model.addAttribute("storageId", storageId);
        model.addAttribute("zoneId", zoneId);
    }

    private String redirectToVirtualSensorInfo(
            Long organizationId,
            Long storageId,
            Long zoneId
    ) {
        return "redirect:/organizations/" + organizationId
                + "/storages/" + storageId
                + "/zones/" + zoneId
                + "/virtual-sensors";
    }
}
