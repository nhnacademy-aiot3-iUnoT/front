package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.organization.client.ZoneThresholdApiClient;
import com.nhnacademy.front.organization.dto.response.ZoneThresholdDetailResponse;
import com.nhnacademy.front.organization.dto.response.ZoneThresholdInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/zones")
public class ZoneThresholdController {
    private final ZoneThresholdApiClient thresholdApiClient;

    @GetMapping("/{zone-id}/zone-thresholds/{zone-threshold-id}")
    public String getZoneSensorDetail(
            @PathVariable(name = "zone-id") Long zoneId,
            @PathVariable(name = "zone-threshold-id") Long zoneThresholdId,
            Model model
    ){
        ZoneThresholdDetailResponse threshold = thresholdApiClient
                .getZoneThreshold(zoneId, zoneThresholdId);

        model.addAttribute("threshold", threshold);

        return "zone-threshold/detail";
    }
}
