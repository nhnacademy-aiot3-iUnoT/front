package com.nhnacademy.front.inventory.controller;

import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.inventory.client.InventoryApiClient;
import com.nhnacademy.front.inventory.dto.response.InventoriesResponse;
import com.nhnacademy.front.inventory.service.InventoryFrontService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryApiClient inventoryApiClient;
    private final InventoryFrontService inventoryFrontService;


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

        model.addAttribute("inventories",result);
        // storages 해당 재고 저장소만 목록 조회
        model.addAttribute("storages",inventoryFrontService.getStorageInfos(result.content()));

        return "inventory/inventory-list";
    }
}
