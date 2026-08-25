package com.nhnacademy.front.inventory.controller;

import com.nhnacademy.front.inventory.client.InventoryApiClient;
import com.nhnacademy.front.inventory.dto.request.InboundMedicineRequest;
import com.nhnacademy.front.organization.client.StorageApiClient;
import com.nhnacademy.front.organization.dto.response.StorageInfoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/inventory")
@Slf4j
public class InboundController {

    private final StorageApiClient storageApiClient;
    private final InventoryApiClient inventoryApiClient;

    private static final String INBOUND_VIEW = "inventory/inbound";

    @GetMapping
    public String getInbound(Model model){


        List<StorageInfoResponse> infoResponseList = storageApiClient.getStoragesInbound();
        log.info("storage info count {} ",infoResponseList.size());

        model.addAttribute("storages",infoResponseList);

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







}
