package com.nhnacademy.front.global.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AddressController {

    @Value("${juso.confirm-key}")
    private String jusoConfirmKey;

    @GetMapping("/juso/popup")
    public String jusoPopup(Model model) {
        model.addAttribute("jusoConfirmKey", jusoConfirmKey);
        return "organization/juso-popup";
    }

    @PostMapping("/juso/popup")
    public String jusoPopupResult(@RequestParam Map<String, String> params, Model model) {
        model.addAttribute("jusoConfirmKey", jusoConfirmKey);

        model.addAttribute("inputYn", params.get("inputYn"));
        model.addAttribute("roadAddrPart1", params.get("roadAddrPart1"));
        model.addAttribute("addrDetail", params.get("addrDetail"));
        model.addAttribute("zipNo", params.get("zipNo"));

        return "organization/juso-popup";
    }
}
