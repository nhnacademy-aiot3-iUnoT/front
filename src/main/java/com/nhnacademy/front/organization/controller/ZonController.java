package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.organization.client.ThresholdZoneApiClient;
import com.nhnacademy.front.organization.client.ZoneApiClient;
import com.nhnacademy.front.organization.dto.response.ThresholdSpecResponse;
import com.nhnacademy.front.organization.dto.response.ZoneInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ZonController {

    private final ZoneApiClient zoneApiClient;
    private final ThresholdZoneApiClient thresholdZoneApiClient;


    // 해당 저장소의 구역 찾기
    @GetMapping("/storages/{storage-id}/zones")
    @ResponseBody
    public List<ZoneInfoResponse> getZones(@PathVariable(name="storage-id")Long storageId){

        return zoneApiClient.getZones(storageId);
    }


    // 해당 구역의 임계 설정 찾기

    @GetMapping("zones/{zone-id}/zone-thresholds")
    @ResponseBody
    public List<ThresholdSpecResponse> getThresholds(@PathVariable(name="zone-id")Long zoneId){

        return thresholdZoneApiClient.getThresholdZones(zoneId);

    }

}
