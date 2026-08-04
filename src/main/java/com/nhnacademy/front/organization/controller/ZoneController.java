package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.global.error.ApiException;
import com.nhnacademy.front.organization.client.RuleEngineApiClient;
import com.nhnacademy.front.organization.dto.response.SensorHistoryResponse;
import com.nhnacademy.front.organization.dto.response.SensorLatestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping(
        "/organizations/{organizationId}" +
                "/storages/{storageId}" +
                "/zones"
)
public class ZoneController {

    private final RuleEngineApiClient ruleEngineApiClient;

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

        return "organization/sensorInfo";
    }
}
