package com.nhnacademy.front.organization.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ExpiringInventoryController {

    @GetMapping("/expiring")
    public String expiringInventoryPage(){
        return "alert/expiring-inventory";
    }
}
