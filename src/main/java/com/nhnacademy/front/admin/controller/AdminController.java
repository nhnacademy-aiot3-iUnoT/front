package com.nhnacademy.front.admin.controller;

import com.nhnacademy.front.admin.client.AdminOrganizationClient;
import com.nhnacademy.front.admin.dto.OrganizationStatus;
import com.nhnacademy.front.admin.dto.request.OrgCreateRequest;
import com.nhnacademy.front.admin.dto.request.OrgSearchRequest;
import com.nhnacademy.front.admin.dto.response.OrgCreateResponse;
import com.nhnacademy.front.admin.dto.response.OrgDetailResponse;
import com.nhnacademy.front.admin.dto.response.OrgSearchResponse;
import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.organization.client.OrganizationApiClient;
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
@RequestMapping("/admin")
public class AdminController {

    private final AdminOrganizationClient adminOrganizationClient;

    /**
     * 조직 관리
     */
    @GetMapping("/organizations")
    public String organizationList(@ModelAttribute OrgSearchRequest request,
                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                   @RequestParam(name = "size", defaultValue = "10") int size,
                                   Model model) {
        PageResponse<OrgSearchResponse> orgList = adminOrganizationClient.getOrgList(request, page, size);

        model.addAttribute("searchTypes", OrganizationStatus.values());
        model.addAttribute("organizations", orgList);
        return "admin/organization-list";
    }

    @PostMapping("/organizations")
    public String createOrganization(@Valid @ModelAttribute OrgCreateRequest request,
                                     BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
           return "admin/organization-create";
        }

        adminOrganizationClient.createOrg(request);

        return "redirect:/admin/organizations";
    }

    @GetMapping("/organizations/new")
    public String createOrganizationForm(Model model) {
        model.addAttribute("orgCreateRequest", OrgCreateRequest.empty());
        return "admin/organization-create";
    }

    @GetMapping("/organizations/{id}")
    public String organizationDetail(@PathVariable Long id, Model model) {
        OrgDetailResponse organization = adminOrganizationClient.getOrgDetail(id);

        model.addAttribute("organization", organization);

        return "admin/organization-detail";
    }

    @DeleteMapping("/organizations/{id}")
    public String deleteOrganization(@PathVariable Long id) {
        adminOrganizationClient.deleteOrg(id);

        return "redirect:/admin/organizations";
    }

    /**
     * 회원 관리
     */
    @GetMapping("/users")
    public String userList() {
        return "/admin/user-list";
    }
}
