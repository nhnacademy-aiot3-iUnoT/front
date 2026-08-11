package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.organization.client.OrganizationApiClient;
import com.nhnacademy.front.organization.dto.request.OrgStatusUpdateRequest;
import com.nhnacademy.front.organization.dto.request.OrgUpdateRequest;
import com.nhnacademy.front.organization.dto.request.OrganizationSetupRequest;
import com.nhnacademy.front.organization.dto.response.OrgDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/organizations")
public class OrganizationController {
    private final OrganizationApiClient orgApiClient;

    @GetMapping("/me")
    public String organizationInfo(Model model) {
        OrgDetailResponse orgInfo = orgApiClient.getOrgInfo();
        model.addAttribute("organization", orgInfo);
        return "organization/org-info";
    }

    @GetMapping("/me/edit")
    public String organizationEditForm(Model model) {
        OrgDetailResponse orgInfo = orgApiClient.getOrgInfo();

        OrgUpdateRequest request = new OrgUpdateRequest(
                orgInfo.roadAddress(),
                orgInfo.zipCode(),
                orgInfo.addressDetail(),
                orgInfo.description()
        );

        model.addAttribute("orgUpdateRequest", request);

        return "organization/org-edit";
    }

    @GetMapping("/me/setup")
    public String organizationSetupForm(Model model) {
        model.addAttribute("orgSetupRequest", new OrganizationSetupRequest());
        return "organization/org-setup";
    }

    @PutMapping("/me/status")
    public String updateOrganizationStatus(@Valid @ModelAttribute("orgStatusUpdateRequest") OrgStatusUpdateRequest request,
                                           BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return "organization/org-info";
        }

        orgApiClient.updateOrgStatus(request);
        return "redirect:/organizations/me";
    }

    @PutMapping("/me/edit")
    public String updateOrganization(@Valid @ModelAttribute("orgEditRequest") OrgUpdateRequest request,
                                     BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return "organization/org-edit";
        }

        orgApiClient.updateOrg(request);

        return "redirect:/organizations/me";
    }

    @PostMapping("/me/setup")
    public String setupOrganization(@Valid @ModelAttribute("orgSetupRequest") OrganizationSetupRequest request,
                                    BindingResult bindingResult) {

        if(bindingResult.hasErrors()) {
            return "organization/org-setup";
        }

        orgApiClient.setupOrg(request);

        return "redirect:/organizations/me";

    }
}
