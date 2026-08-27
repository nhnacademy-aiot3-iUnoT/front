package com.nhnacademy.front.inventory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EnvironmentEventViewController {

    @GetMapping("/zones/{zone-id}/environment-events")
    public String environmentEventPage(@PathVariable(name = "zone-id") Long zoneId, Model model){
        model.addAttribute("zoneId", zoneId);
        return "zone/environment-event-list";
    }
}
