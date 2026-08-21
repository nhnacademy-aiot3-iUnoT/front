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

    @GetMapping("/{organization-id}")
    public String organizationDetail(@PathVariable(name = "organization-id") Long organizationId, Model model) {
        AdminOrgDetailResponse organization = adminOrganizationApiClient.getOrgDetail(organizationId);

        model.addAttribute("organization", organization);

        return "admin/organization-detail";
    }

    @DeleteMapping("/{organization-id}")
    public String deleteOrganization(@PathVariable("organization-id") Long id) {
        adminOrganizationApiClient.deleteOrg(id);

        return "redirect:/admin/organizations";
    }

    /**
     * Owner 초대 관리
     */
    @PostMapping("/{organization-id}/invitations/{invitation-id}/resend")
    public String resendInvitation(@PathVariable(name = "organization-id") Long organizationId,
                                   @PathVariable(name = "invitation-id") Long invitationId) {
        adminOrganizationApiClient.resendInvitation(organizationId, invitationId);

        return "redirect:/admin/organizations/" + organizationId;
    }

    @PostMapping("/{organization-id}/invitations/{invitation-id}/cancel")
    public String cancelInvitation(@PathVariable(name = "organization-id") Long organizationId,
                                   @PathVariable(name = "invitation-id") Long invitationId) {
        adminOrganizationApiClient.cancelInvitation(organizationId, invitationId);

        return "redirect:/admin/organizations/" + organizationId;
    }

    @PostMapping("/{organization-id}/invitations/{invitation-id}/reissue")
    public String reissueInvitation(@PathVariable(name = "organization-id") Long organizationId,
                                    @PathVariable(name = "invitation-id") Long invitationId) {
        adminOrganizationApiClient.reissueInvitation(organizationId, invitationId);

        return "redirect:/admin/organizations/" + organizationId;
    }
}
