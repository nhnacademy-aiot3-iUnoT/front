package com.nhnacademy.front.inventory.controller;

import com.nhnacademy.front.inventory.client.InventoryApiClient;
import com.nhnacademy.front.inventory.dto.request.InboundMedicineRequest;
import com.nhnacademy.front.organization.client.OrganizationApiClient;
import com.nhnacademy.front.organization.client.StorageApiClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class InboundController {

    private final StorageApiClient storageApiClient;
    private final InventoryApiClient inventoryApiClient;

    private static final String INBOUND_VIEW = "inventory/inbound";

    @GetMapping
    public String getInbound(Model model){

        model.addAttribute("storages",storageApiClient.getStorages());

        model.addAttribute("selectedStorageId",null);
        model.addAttribute("selectedZoneId",null);
        model.addAttribute("inboundMedicineRequest", InboundMedicineRequest.from(null,null));


        return INBOUND_VIEW;
    }



    @PostMapping
    public String inbound(@Valid @ModelAttribute InboundMedicineRequest request, BindingResult bindingResult){

        if(bindingResult.hasErrors()){
            return INBOUND_VIEW;
        }

        inventoryApiClient.inbound(request);
        return "redirect:/inventories";
    }

    // url 전체 재고조회 쪽으로 이동 경로 아직 미정






}
