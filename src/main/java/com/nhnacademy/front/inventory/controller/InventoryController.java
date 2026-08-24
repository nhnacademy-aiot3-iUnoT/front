package com.nhnacademy.front.inventory.controller;

import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.inventory.client.InventoryApiClient;
import com.nhnacademy.front.inventory.dto.response.InventoriesResponse;

import com.nhnacademy.front.inventory.dto.response.InventoryInfoResponse;
import com.nhnacademy.front.inventory.service.InventoryFrontService;

import com.nhnacademy.front.medicine.client.MedicineInfoApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryApiClient inventoryApiClient;
    private final InventoryFrontService inventoryFrontService;
    private final MedicineInfoApiClient medicineInfoApiClient;


    // 전체 재고 조회
    @GetMapping("/inventories")
    public String getInventories(
            @RequestParam(name="search",required = false) String search,
            @RequestParam(name="storage-id",required = false)Long storageId,
            @RequestParam(name="page", defaultValue = "0")int page,
            @RequestParam(name="size",defaultValue = "10")int size,
            Model model
    ) {

        PageResponse<InventoriesResponse> result = inventoryApiClient.getInventories(search, storageId, page, size);

        model.addAttribute("inventories",result.content());
        // storages 해당 재고 저장소만 목록 조회
        model.addAttribute("storages",inventoryFrontService.getStorageInfos(result.content()));

        return "inventory/inventory-list";
    }


    // 상세 재고 조회
    @GetMapping("/storages/{storage-id}/pack-units/{medicine-pack-unit-id}")
    public String getInventoryInfo(
            @PathVariable(name="storage-id") Long storageId,
            @PathVariable(name="medicine-pack-unit-id")Long packUnitId,
            @RequestParam(name="page",defaultValue= "0")int page,
            @RequestParam(name="size",defaultValue="10")int size,
            Model model
    ){


        InventoryInfoResponse result = inventoryApiClient.getInventoryInfo(storageId,packUnitId,page,size);


        model.addAttribute("medicineInfo",medicineInfoApiClient.getMedicine(packUnitId));
        model.addAttribute("inventory",result);
        model.addAttribute("inventories",result.inventories().content());

        model.addAttribute("currentPage",result.inventories().content());
        model.addAttribute("totalPages",result.inventories().totalPages());
        model.addAttribute("totalElements",result.inventories().totalElements());
        model.addAttribute("pageSize",result.inventories().size());

        model.addAttribute("totalQuantity",inventoryFrontService.getStorageQuantity(result.inventories()));

        return "inventory/inventory-detail";
    }



}
