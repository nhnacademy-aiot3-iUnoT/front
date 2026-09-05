package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.organization.client.OrganizationMemberApiClient;
import com.nhnacademy.front.organization.client.ZoneApiClient;
import com.nhnacademy.front.organization.client.ZoneSensorApiClient;
import com.nhnacademy.front.organization.client.ZoneThresholdApiClient;
import com.nhnacademy.front.organization.dto.OrganizationRole;
import com.nhnacademy.front.organization.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/storages")
public class ZoneController {
    private final ZoneApiClient zoneApiClient;
    private final ZoneThresholdApiClient thresholdApiClient;
    private final ZoneSensorApiClient sensorApiClient;
    private final OrganizationMemberApiClient organizationMemberApiClient;


    @GetMapping("/{storage-id}/zones/{zone-id}")
    public String getZoneDetail(
            @PathVariable(name = "storage-id") Long storageId,
            @PathVariable(name = "zone-id") Long zoneId,
            Model model
    ){
        ZoneDetailResponse zone = zoneApiClient.getZone(storageId, zoneId);
        List<ZoneThresholdInfoResponse> thresholdList = thresholdApiClient
                .getZoneThresholds(zoneId);
        List<ZoneSensorInfoResponse> sensorList = sensorApiClient
                .getZoneThresholds(zoneId);
        OrganizationMemberRoleResponse roleResponse = organizationMemberApiClient.getRole();
        boolean canManage = (
                roleResponse.role() == OrganizationRole.ORG_BOSS ||
                        roleResponse.role() == OrganizationRole.ORG_OWNER
        );

        model.addAttribute("zone", zone);
        model.addAttribute("thresholdList", thresholdList);
        model.addAttribute("sensorList", sensorList);
        model.addAttribute("canManage", canManage);

        return "zone/zone-detail";
    }





    // 해당 저장소의 구역 찾기
    @GetMapping("/{storage-id}/zones")
    @ResponseBody
    public List<ZoneInfoResponse> getZones(@PathVariable(name="storage-id")Long storageId){

        return zoneApiClient.getZones(storageId);
    }



}
