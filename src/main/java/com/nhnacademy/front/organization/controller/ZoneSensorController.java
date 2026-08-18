package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.organization.client.ZoneSensorApiClient;
import com.nhnacademy.front.organization.dto.response.ZoneSensorDetailResponse;
import com.nhnacademy.front.organization.dto.response.ZoneSensorInfoResponse;
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
public class ZoneSensorController {
    private final ZoneSensorApiClient sensorApiClient;

    @GetMapping("/{zone-id}/zone-sensors/{zone-sensor-id}")
    public String getZoneSensorDetail(
            @PathVariable(name = "zone-id") Long zoneId,
            @PathVariable(name = "zone-sensor-id") Long zoneSensorId,
            Model model
    ){
        ZoneSensorDetailResponse sensor = sensorApiClient
                .getZoneThreshold(zoneId, zoneSensorId);

        model.addAttribute("sensor", sensor);

        return "zone-sensor/detail";
    }
}
