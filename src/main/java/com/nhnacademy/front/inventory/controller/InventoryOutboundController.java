package com.nhnacademy.front.inventory.controller;

import com.nhnacademy.front.inventory.client.InventoryApiClient;
import com.nhnacademy.front.inventory.dto.request.MedicineOutboundRequest;
import com.nhnacademy.front.inventory.dto.response.MedicineOutboundTargetResponse;
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
public class InventoryOutboundController {

    private final InventoryApiClient inventoryApiClient;

    @GetMapping("/{inventory-id}/outbound")
    public String getOutboundForm(
            @PathVariable(name = "inventory-id") Long inventoryId,
            Model model
    ) {
        MedicineOutboundTargetResponse target =
                inventoryApiClient.getOutboundTarget(inventoryId);

        model.addAttribute("target", target);

        if (!model.containsAttribute("outboundRequest")) {
            model.addAttribute(
                    "outboundRequest",
                    new MedicineOutboundRequest(
                            target.medicinePackageUnitId(),
                            null,
                            target.zoneId(),
                            null,
                            null
                    )
            );
        }

        return "inventory/outbound";
    }

    @PostMapping("/{inventory-id}/outbound")
    public String outbound(
            @PathVariable(name = "inventory-id") Long inventoryId,
            @Valid @ModelAttribute("outboundRequest")
            MedicineOutboundRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        boolean memoRequired =
                "STORAGE_TRANSFER".equals(request.reason())
                        || "OTHER".equals(request.reason());

        if (memoRequired
                && (request.memo() == null
                || request.memo().isBlank())) {

            String message =
                    "STORAGE_TRANSFER".equals(request.reason())
                            ? "이관할 저장소 또는 부서를 입력해주세요."
                            : "구체적인 출고 사유를 입력해주세요.";

            bindingResult.rejectValue(
                    "memo",
                    "required.outboundRequest.memo",
                    message
            );
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "target",
                    inventoryApiClient.getOutboundTarget(inventoryId)
            );

            return "inventory/outbound";
        }

        inventoryApiClient.outbound(inventoryId, request);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "출고 처리가 완료되었습니다."
        );

        return "redirect:/inventories";
    }
}