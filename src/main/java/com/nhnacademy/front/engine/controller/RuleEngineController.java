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
    /*
        가상 센서데이터 생성 화면
     */
    @GetMapping("/me/storages/{storage-id}/zones/{zone-id}/virtual-sensors/create")
    public String showCreateVirtualSensorForm(
            @PathVariable("storage-id") Long storageId,
            @PathVariable("zone-id") Long zoneId,
            Model model
    ) {
        addZoneAttributes(model, storageId, zoneId);
        model.addAttribute("edit", false);

        return VIRTUAL_SENSOR_FORM_VIEW;
    }

    /*
        가상 센서데이터 수정 화면
     */
    @GetMapping("/me/storages/{storage-id}/zones/{zone-id}/virtual-sensors/edit")
    public String showUpdateVirtualSensorForm(
            @PathVariable("storage-id") Long storageId,
            @PathVariable("zone-id") Long zoneId,
            Model model
    ) {
        addZoneAttributes(model, storageId, zoneId);
        model.addAttribute("edit", true); // 수정모드

        VirtualSensorInfoResponse virtualSensor =
                ruleEngineApiClient.getVirtualSensorData(zoneId);

        model.addAttribute("virtualSensor", virtualSensor);

        return VIRTUAL_SENSOR_FORM_VIEW;
    }

    /*
        가상 센서데이터 생성
     */
    @PostMapping("/me/storages/{storage-id}/zones/{zone-id}/virtual-sensors")
    public String createVirtualSensor(
            @PathVariable("storage-id") Long storageId,
            @PathVariable("zone-id") Long zoneId,
            @Valid @ModelAttribute VirtualSensorCreateRequest request,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            addZoneAttributes(model, storageId, zoneId);
            model.addAttribute("edit", false); // 생성모드

            return VIRTUAL_SENSOR_FORM_VIEW;
        }

        ruleEngineApiClient.createVirtualSensorData(zoneId, request);

        return redirectToVirtualSensorInfo(storageId, zoneId);
    }

    /*
        가상 센서 데이터 업데이트
     */
    @PutMapping("/me/storages/{storage-id}/zones/{zone-id}/virtual-sensors")
    public String updateVirtualSensor(
            @PathVariable("storage-id") Long storageId,
            @PathVariable("zone-id") Long zoneId,
            @Valid @ModelAttribute VirtualSensorUpdateRequest request,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            addZoneAttributes(model, storageId, zoneId);
            model.addAttribute("edit", true); // 수정모드
            model.addAttribute(
                    "virtualSensor",
                    ruleEngineApiClient.getVirtualSensorData(zoneId)
            );

            return VIRTUAL_SENSOR_FORM_VIEW;
        }

        ruleEngineApiClient.updateVirtualSensorData(zoneId, request);

        return redirectToVirtualSensorInfo(storageId, zoneId);
    }

    /*
        가상 센서 삭제
     */
    @DeleteMapping("/me/storages/{storage-id}/zones/{zone-id}/virtual-sensors")
    public String deleteVirtualSensor(
            @PathVariable("storage-id") Long storageId,
            @PathVariable("zone-id") Long zoneId
    ) {
        ruleEngineApiClient.deleteVirtualSensorData(zoneId);

        return "redirect:" + zoneBasePath(storageId, zoneId) + "/sensorInfo";
    }

    /*
        가상 센서데이터 설정 정보 화면
     */
    @GetMapping("/me/storages/{storage-id}/zones/{zone-id}/virtual-sensors")
    public String virtualSensorInfo(
            @PathVariable("storage-id") Long storageId,
            @PathVariable("zone-id") Long zoneId,
            Model model
    ) {
        addZoneAttributes(model, storageId, zoneId);

        try {
            VirtualSensorInfoResponse virtualSensor =
                    ruleEngineApiClient.getVirtualSensorData(zoneId);

            model.addAttribute("virtualSensor", virtualSensor);

        } catch (ApiException e) {
            // 아직 가상 센서를 만들지 않은 Zone(V001)은 오류가 아니라 '미등록' 상태로 화면에서 안내한다.
            // 그 외에는 화면을 열지 않고 error.html이 안내하도록 그대로 둔다.
            if (e.getErrorCode() != ErrorCode.V001) {
                throw e;
            }
        }

        return VIRTUAL_SENSOR_INFO_VIEW;
    }

    /*
        가상 센서 활성화 / 비활성화
     */
    @PutMapping("/me/storages/{storage-id}/zones/{zone-id}/virtual-sensors/status")
    public String changeVirtualSensorStatus(
            @PathVariable("storage-id") Long storageId,
            @PathVariable("zone-id") Long zoneId,
            @Valid @ModelAttribute VirtualSensorStatusRequest request,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return redirectToVirtualSensorInfo(storageId, zoneId);
        }

        ruleEngineApiClient.changeVirtualSensorStatus(zoneId, request);

        return redirectToVirtualSensorInfo(storageId, zoneId);
    }

    /*
        zone의 센서의 상세 데이터 정보 화면
        (센서 데이터 조회는 zoneId만 필요하므로 조직 정보를 따로 조회하지 않는다)
     */
    @GetMapping("/me/storages/{storage-id}/zones/{zone-id}/sensorInfo")
    public String info(
            @PathVariable("storage-id") Long storageId,
            @PathVariable("zone-id") Long zoneId,
            Model model
    ) {
        addZoneAttributes(model, storageId, zoneId);

        try {
            List<SensorLatestResponse> latestSensors =
                    ruleEngineApiClient.getLatestSensors(zoneId);

            model.addAttribute("latestSensors", latestSensors);

        } catch (ApiException e) {
            // 남의 조직이거나 없는 구역이면 화면을 열지 않고 error.html이 안내하도록 다시 던진다.
            if (isNotAccessible(e)) {
                throw e;
            }

            model.addAttribute("latestSensors", List.of());
            model.addAttribute("latestSensorsErrorMessage", e.getMessage());
        }


        try {
            List<SensorHistoryResponse> sensorHistory =
                    ruleEngineApiClient.getSensorHistory(zoneId);

            model.addAttribute("sensorHistory", sensorHistory);

        } catch (ApiException e) {
            // 남의 조직이거나 없는 구역이면 화면을 열지 않고 error.html이 안내하도록 다시 던진다.
            if (isNotAccessible(e)) {
                throw e;
            }

            model.addAttribute("sensorHistory", List.of());
            model.addAttribute("sensorHistoryErrorMessage", e.getMessage());
        }

        return SENSOR_INFO_VIEW;
    }

    /*
        남의 조직이거나 없는 구역/창고인지 판단한다.
        인벤토리가 권한 없음은 G002, 대상 없음은 S001/Z001로 답한다.
     */
    private boolean isNotAccessible(ApiException e) {
        ErrorCode errorCode = e.getErrorCode();

        return errorCode == ErrorCode.G002
                || errorCode == ErrorCode.S001
                || errorCode == ErrorCode.Z001;
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
