package com.nhnacademy.front.admin.controller;

import com.nhnacademy.front.admin.client.AdminOrganizationApiClient;
import com.nhnacademy.front.admin.dto.OrganizationStatus;
import com.nhnacademy.front.admin.dto.request.OrgCreateRequest;
import com.nhnacademy.front.admin.dto.request.OrgSearchRequest;
import com.nhnacademy.front.admin.dto.response.AdminOrgDetailResponse;
import com.nhnacademy.front.admin.dto.response.OrgSearchResponse;
import com.nhnacademy.front.global.dto.PageResponse;
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
@RequestMapping("/admin/organizations")
public class AdminOrganizationController {

    private final AdminOrganizationApiClient adminOrganizationApiClient;

    /**
     * 조직 관리
     */
    @GetMapping
    public String organizationList(@ModelAttribute OrgSearchRequest request,
                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                   @RequestParam(name = "size", defaultValue = "10") int size,
                                   Model model) {
        PageResponse<OrgSearchResponse> orgList = adminOrganizationApiClient.getOrgList(request, page, size);

        model.addAttribute("searchTypes", OrganizationStatus.values());
        model.addAttribute("organizations", orgList);
        return "admin/organization-list";
    }

    @PostMapping
    public String createOrganization(@Valid @ModelAttribute OrgCreateRequest request,
                                     BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
           return "admin/organization-create";
        }

        adminOrganizationApiClient.createOrg(request);

        return "redirect:/admin/organizations";
    }

    @GetMapping("/new")
    public String createOrganizationForm(Model model) {
        model.addAttribute("orgCreateRequest", new OrgCreateRequest());
        return "admin/organization-create";
    }

    @GetMapping("/{id}")
    public String organizationDetail(@PathVariable Long id, Model model) {
        AdminOrgDetailResponse organization = adminOrganizationApiClient.getOrgDetail(id);

        model.addAttribute("organization", organization);

        return "admin/organization-detail";
    }

    @DeleteMapping("/{id}")
    public String deleteOrganization(@PathVariable Long id) {
        adminOrganizationApiClient.deleteOrg(id);

        return "redirect:/admin/organizations";
    }

    /**
     * Owner 초대 관리
     */
    @PostMapping("/{organizationId}/invitations/{invitationId}/resend")
    public String resendInvitation(@PathVariable Long organizationId, @PathVariable Long invitationId) {
        adminOrganizationApiClient.resendInvitation(organizationId, invitationId);

        return "redirect:/admin/organizations/" + organizationId;
    }

    @PostMapping("/{organizationId}/invitations/{invitationId}/cancel")
    public String cancelInvitation(@PathVariable Long organizationId, @PathVariable Long invitationId) {
        adminOrganizationApiClient.cancelInvitation(organizationId, invitationId);

        return "redirect:/admin/organizations/" + organizationId;
    }

    @PostMapping("/{organizationId}/invitations/{invitationId}/reissue")
    public String reissueInvitation(@PathVariable Long organizationId, @PathVariable Long invitationId) {
        adminOrganizationApiClient.reissueInvitation(organizationId, invitationId);

        return "redirect:/admin/organizations/" + organizationId;
    }
}
