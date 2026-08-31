package com.nhnacademy.front.inventory.controller;

import com.nhnacademy.front.inventory.client.InventoryApiClient;
import com.nhnacademy.front.inventory.dto.request.MedicineDisposalRequest;
import com.nhnacademy.front.inventory.dto.response.MedicineDisposalTargetResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/inventories")
public class InventoryDisposalController {

    private final InventoryApiClient inventoryApiClient;

    @GetMapping("/{inventory-id}/disposal")
    public String getDisposalForm(
            @PathVariable(name = "inventory-id") Long inventoryId,
            Model model
    ) {
        MedicineDisposalTargetResponse target =
                inventoryApiClient.getDisposalTarget(inventoryId);

        model.addAttribute("target", target);

        if (!model.containsAttribute("disposalRequest")) {
            model.addAttribute(
                    "disposalRequest",
                    new MedicineDisposalRequest()
            );
        }

        return "inventory/disposal";
    }

    @PostMapping("/{inventory-id}/disposal")
    public String dispose(
            @PathVariable(name = "inventory-id") Long inventoryId,
            @Valid @ModelAttribute("disposalRequest")
            MedicineDisposalRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "target",
                    inventoryApiClient.getDisposalTarget(inventoryId)
            );

            return "inventory/disposal";
        }

        inventoryApiClient.dispose(inventoryId, request);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "폐기 처리가 완료되었습니다."
        );

        return "redirect:/inventories";
    }
}
