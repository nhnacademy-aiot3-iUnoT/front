package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.organization.client.OrganizationMemberApiClient;
import com.nhnacademy.front.organization.dto.OrganizationRole;
import com.nhnacademy.front.organization.dto.response.OrganizationMemberRoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AlertViewController {

    private final OrganizationMemberApiClient organizationMemberApiClient;

    @GetMapping("alerts")
    public String alertsPage(Model model){
        OrganizationMemberRoleResponse roleResponse = organizationMemberApiClient.getRole();
        boolean canManage = (
                roleResponse.role() == OrganizationRole.ORG_BOSS ||
                        roleResponse.role() == OrganizationRole.ORG_OWNER
        );

        model.addAttribute("canManage", canManage);

        return "alert/list";
    }
}
