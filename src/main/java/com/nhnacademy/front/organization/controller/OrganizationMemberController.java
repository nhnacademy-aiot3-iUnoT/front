package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.organization.client.DepartmentApiClient;
import com.nhnacademy.front.organization.client.OrganizationApiClient;
import com.nhnacademy.front.organization.client.OrganizationMemberApiClient;
import com.nhnacademy.front.organization.dto.OrganizationRole;
import com.nhnacademy.front.organization.dto.request.OrganizationMemberSearchRequest;
import com.nhnacademy.front.organization.dto.request.MemberDepartmentUpdateRequest;
import com.nhnacademy.front.organization.dto.request.OrganizationRoleUpdateRequest;
import com.nhnacademy.front.organization.dto.response.OrgDetailResponse;
import com.nhnacademy.front.organization.dto.response.DepartmentListResponse;
import com.nhnacademy.front.organization.dto.response.OrganizationMemberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/organizations/me/members")
public class OrganizationMemberController {
    private final OrganizationMemberApiClient organizationMemberApiClient;
    private final OrganizationApiClient organizationApiClient;
    private final DepartmentApiClient departmentApiClient;

    @GetMapping
    public String memberList(@ModelAttribute OrganizationMemberSearchRequest request,
                             @RequestParam(defaultValue = "false") boolean withoutDepartment,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Model model) {
        PageResponse<OrganizationMemberResponse> members = organizationMemberApiClient.getMembers(request, withoutDepartment, page, size);
        OrgDetailResponse organization = organizationApiClient.getOrgInfo();

        model.addAttribute("members", members);
        model.addAttribute("organization", organization);
        model.addAttribute("organizationRoles", OrganizationRole.values());
        model.addAttribute("withoutDepartment", withoutDepartment);
        model.addAttribute("departments", getActiveDepartments());

        return "organization/member-list";
    }

    @PutMapping("/{member-id}/role")
    public String updateRole(@PathVariable(name = "member-id") Long memberId,
                             @Valid @ModelAttribute OrganizationRoleUpdateRequest request,
                             BindingResult bindingResult,
                             @RequestParam(defaultValue = "false") boolean withoutDepartment) {
        if (!bindingResult.hasErrors()) {
            organizationMemberApiClient.updateRole(memberId, request);
        }

        return "redirect:/organizations/me/members?withoutDepartment=" + withoutDepartment;
    }

    @DeleteMapping("/{member-id}")
    public String deleteMember(@PathVariable(name = "member-id") Long memberId,
                               @RequestParam(defaultValue = "false") boolean withoutDepartment) {
        organizationMemberApiClient.deleteMember(memberId);
        return "redirect:/organizations/me/members?withoutDepartment=" + withoutDepartment;
    }

    @PutMapping("/departments")
    public String updateMemberDepartments(@RequestParam Long memberId,
                                          @RequestParam(required = false) List<Long> departmentIds,
                                          @RequestParam(defaultValue = "false") boolean withoutDepartment) {
        organizationMemberApiClient.updateMemberDepartments(memberId, new MemberDepartmentUpdateRequest(departmentIds));
        return "redirect:/organizations/me/members?withoutDepartment=" + withoutDepartment;
    }

    @GetMapping("/{member-id}/departments")
    @ResponseBody
    public List<DepartmentListResponse> getMemberDepartments(@PathVariable(name = "member-id") Long memberId) {
        return organizationMemberApiClient.getMemberDepartments(memberId);
    }

    private List<DepartmentListResponse> getActiveDepartments() {
        return departmentApiClient.getDepartments().stream()
                .filter(department -> department.status().name().equals("ACTIVE"))
                .toList();
    }
}
