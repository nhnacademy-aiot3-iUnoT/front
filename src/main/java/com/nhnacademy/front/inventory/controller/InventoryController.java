package com.nhnacademy.front.inventory.controller;

import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.inventory.client.InventoryApiClient;
import com.nhnacademy.front.inventory.dto.response.InventoriesResponse;
import com.nhnacademy.front.inventory.dto.response.InventoryDetailResponse;
import com.nhnacademy.front.inventory.dto.response.InventoryInfoResponse;
import com.nhnacademy.front.inventory.service.InventoryFrontService;
import com.nhnacademy.front.medicine.client.MedicineInfoApiClient;
import com.nhnacademy.front.organization.client.DepartmentApiClient;
import com.nhnacademy.front.organization.client.OrganizationMemberApiClient;
import com.nhnacademy.front.organization.dto.OrganizationRole;
import com.nhnacademy.front.organization.dto.response.DepartmentListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryApiClient inventoryApiClient;
    private final InventoryFrontService inventoryFrontService;
    private final MedicineInfoApiClient medicineInfoApiClient;
    private static final int PAGE_GROUP_SIZE = 10;
    private final OrganizationMemberApiClient organizationMemberApiClient;
    private final DepartmentApiClient departmentApiClient;



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


        int currentPage = result.page();
        int totalPages = result.totalPages();


        int startPage = (currentPage / PAGE_GROUP_SIZE) * PAGE_GROUP_SIZE;

        int endPage = totalPages == 0 ? 0 : Math.min(
                        startPage + PAGE_GROUP_SIZE - 1,
                        totalPages - 1);


        List<String> departmentNames = departmentApiClient.getMyDepartments().stream()
                        .map(DepartmentListResponse::name)
                                .toList();


        boolean isBoss = organizationMemberApiClient.getRole().role() == OrganizationRole.ORG_BOSS;

        model.addAttribute("isBoss",isBoss);
        model.addAttribute("departmentNames",departmentNames);


        model.addAttribute("inventories",result.content());
        // storages 해당 재고 저장소만 목록 조회
        model.addAttribute("storages",inventoryFrontService.getStorageInfos(result.content()));


        model.addAttribute("currentPage",currentPage);
        model.addAttribute("totalPages",totalPages);
        model.addAttribute("totalElements",result.totalElements());
        model.addAttribute("pageSize",result.size());

        model.addAttribute("startPage",startPage);
        model.addAttribute("endPage",endPage);

        model.addAttribute("search",search);
        model.addAttribute("selectedStorageId",storageId);


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
        PageResponse<InventoryDetailResponse> inventories = result.inventories();


        int currentPage = inventories.page();
        int totalPages = inventories.totalPages();

        int startPage = (currentPage / PAGE_GROUP_SIZE) * PAGE_GROUP_SIZE;

        int endPage = totalPages == 0 ? 0 : Math.min(
                startPage + PAGE_GROUP_SIZE - 1,
                totalPages - 1);

        var medicineInfo = medicineInfoApiClient.getMedicine(packUnitId);
        String narcoticKindCode = medicineInfo.narcoticKindCode();

        boolean isRestrictedNarcotic =
                "마약".equals(narcoticKindCode)
                        || "향정".equals(narcoticKindCode)
                        || "향정신성의약품".equals(narcoticKindCode);

        OrganizationRole role = organizationMemberApiClient.getRole().role();

        model.addAttribute(
                "canManageNarcotics",
                role == OrganizationRole.ORG_OWNER
                        || role == OrganizationRole.ORG_BOSS
        );
        model.addAttribute("isRestrictedNarcotic", isRestrictedNarcotic);
        model.addAttribute("medicineInfo", medicineInfo);
        model.addAttribute("inventory",result);
        model.addAttribute("inventories",inventories.content());

        model.addAttribute("currentPage",currentPage);
        model.addAttribute("totalPages",totalPages);
        model.addAttribute("totalElements",result.inventories().totalElements());
        model.addAttribute("pageSize",result.inventories().size());

        model.addAttribute("startPage",startPage);
        model.addAttribute("endPage",endPage);


        model.addAttribute("totalQuantity",inventoryFrontService.getStorageQuantity(result.inventories()));

        return "inventory/inventory-detail";
    }



}
