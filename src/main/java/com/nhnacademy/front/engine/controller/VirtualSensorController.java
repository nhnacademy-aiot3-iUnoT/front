package com.nhnacademy.front.engine.controller;

import com.nhnacademy.front.engine.client.RuleEngineApiClient;
import com.nhnacademy.front.engine.dto.request.VirtualSensorCreateRequest;
import com.nhnacademy.front.engine.dto.request.VirtualSensorStatusRequest;
import com.nhnacademy.front.engine.dto.request.VirtualSensorUpdateRequest;
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
    조직관리 탭의 가상 센서 관리 화면.
    여기서는 deviceEui만 만든다. 어느 구역에서 측정되는지는 그 deviceEui를 구역의 센서로
    등록하는 순간 정해지고, 등록 전에 만들어진 데이터는 갈 곳이 없어 버려진다.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/organizations/me/virtual-sensors")
public class VirtualSensorController {

    private static final String LIST_VIEW = "organization/virtualsensor-list";
    private static final String FORM_VIEW = "organization/virtualsensor-create";
    private static final String INFO_VIEW = "organization/virtualsensor-info";

    private static final String LIST_PATH = "redirect:/organizations/me/virtual-sensors";

    private final RuleEngineApiClient ruleEngineApiClient;
    private final OrganizationApiClient organizationApiClient;

    /*
        가상 센서 목록 화면
     */
    @GetMapping
    public String virtualSensors(Model model) {
        List<VirtualSensorInfoResponse> virtualSensors =
                ruleEngineApiClient.getVirtualSensors(myOrganizationId());

        model.addAttribute("virtualSensors", virtualSensors);

        return LIST_VIEW;
    }

    /*
        가상 센서 생성 화면
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("edit", false);

        return FORM_VIEW;
    }

    /*
        가상 센서 생성
     */
    @PostMapping
    public String createVirtualSensor(
            @Valid @ModelAttribute VirtualSensorCreateRequest request,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("edit", false);

            return FORM_VIEW;
        }

        ruleEngineApiClient.createVirtualSensorData(myOrganizationId(), request);

        return LIST_PATH;
    }

    /*
        가상 센서 설정 정보 화면
     */
    @GetMapping("/{device-eui}")
    public String virtualSensorInfo(
            @PathVariable("device-eui") String deviceEui,
            Model model
    ) {
        model.addAttribute("deviceEui", deviceEui);
        model.addAttribute(
                "virtualSensor",
                ruleEngineApiClient.getVirtualSensorData(myOrganizationId(), deviceEui)
        );

        return INFO_VIEW;
    }

    /*
        가상 센서 수정 화면
     */
    @GetMapping("/{device-eui}/edit")
    public String showUpdateForm(
            @PathVariable("device-eui") String deviceEui,
            Model model
    ) {
        VirtualSensorInfoResponse virtualSensor =
                ruleEngineApiClient.getVirtualSensorData(myOrganizationId(), deviceEui);

        // 수정할 설정이 없으면 목록으로 돌려보낸다.
        if (!virtualSensor.registered()) {
            return LIST_PATH;
        }

        model.addAttribute("edit", true);
        model.addAttribute("deviceEui", deviceEui);
        model.addAttribute("virtualSensor", virtualSensor);

        return FORM_VIEW;
    }

    /*
        가상 센서 수정
     */
    @PutMapping("/{device-eui}")
    public String updateVirtualSensor(
            @PathVariable("device-eui") String deviceEui,
            @Valid @ModelAttribute VirtualSensorUpdateRequest request,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("edit", true);
            model.addAttribute("deviceEui", deviceEui);
            model.addAttribute(
                    "virtualSensor",
                    ruleEngineApiClient.getVirtualSensorData(myOrganizationId(), deviceEui)
            );

            return FORM_VIEW;
        }

        ruleEngineApiClient.updateVirtualSensorData(myOrganizationId(), deviceEui, request);

        return LIST_PATH + "/" + deviceEui;
    }

    /*
        가상 센서 삭제
     */
    @DeleteMapping("/{device-eui}")
    public String deleteVirtualSensor(
            @PathVariable("device-eui") String deviceEui
    ) {
        ruleEngineApiClient.deleteVirtualSensorData(myOrganizationId(), deviceEui);

        return LIST_PATH;
    }

    /*
        가상 센서 활성화 / 비활성화
     */
    @PutMapping("/{device-eui}/status")
    public String changeVirtualSensorStatus(
            @PathVariable("device-eui") String deviceEui,
            @Valid @ModelAttribute VirtualSensorStatusRequest request,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return LIST_PATH;
        }

        ruleEngineApiClient.changeVirtualSensorStatus(myOrganizationId(), deviceEui, request);

        return LIST_PATH;
    }

    /*
        룰엔진은 가상 센서 URL에 조직 ID를 요구하므로 로그인한 사용자의 조직을 조회해서 채운다.
     */
    private Long myOrganizationId() {
        return organizationApiClient.getOrgInfo().id();
    }
}
