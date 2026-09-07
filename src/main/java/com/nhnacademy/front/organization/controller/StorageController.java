package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.inventory.client.StockThresholdApiClient;
import com.nhnacademy.front.inventory.dto.response.StockThresholdInfoResponse;
import com.nhnacademy.front.organization.client.*;
import com.nhnacademy.front.organization.dto.OrganizationRole;
import com.nhnacademy.front.organization.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/storages")
public class StorageController {

    private final StorageApiClient storageApiClient;
    private final ZoneApiClient zoneApiClient;
    private final StockThresholdApiClient stockThresholdApiClient;
    private final OrganizationMemberApiClient organizationMemberApiClient;
    private final StorageDepartmentApiClient storageDepartmentApiClient;

    @GetMapping
    public String getStorages(
            Model model
    ){
        List<StorageInfoResponse> storageList = storageApiClient.getStorages();
        OrganizationMemberRoleResponse roleResponse = organizationMemberApiClient.getRole();
        boolean canManage = (
                roleResponse.role() == OrganizationRole.ORG_BOSS ||
                roleResponse.role() == OrganizationRole.ORG_OWNER
        );

        model.addAttribute("storageList", storageList);
        model.addAttribute("canManage", canManage);

        return "storage/storage-list";
    }

    @GetMapping("/{storage-id}")
    public String getStorageDetail(
            @PathVariable(name = "storage-id") Long storageId,
            Model model
    ){
        StorageDetailResponse storage = storageApiClient.getStorage(storageId);
        List<ZoneInfoResponse> zoneList = zoneApiClient.getZones(storageId);
        List<StockThresholdInfoResponse> stockThresholdList = stockThresholdApiClient.getStockThresholds(storageId);
        List<DepartmentByStorageResponse> departmentList = storageDepartmentApiClient.getDepartmentsByStorageId(storageId);
        OrganizationMemberRoleResponse roleResponse = organizationMemberApiClient.getRole();
        boolean canManage = (
                roleResponse.role() == OrganizationRole.ORG_BOSS ||
                        roleResponse.role() == OrganizationRole.ORG_OWNER
        );

        model.addAttribute("storage", storage);
        model.addAttribute("zoneList", zoneList);
        model.addAttribute("departmentList", departmentList);
        model.addAttribute("stockThresholdList", stockThresholdList);
        model.addAttribute("canManage", canManage);

        return "storage/storage-detail";
    }
}
