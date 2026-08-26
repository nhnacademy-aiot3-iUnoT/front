package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.organization.client.DepartmentApiClient;
import com.nhnacademy.front.organization.client.MemberDepartmentApiClient;
import com.nhnacademy.front.organization.client.OrganizationApiClient;
import com.nhnacademy.front.organization.client.OrganizationMemberApiClient;
import com.nhnacademy.front.organization.dto.OrganizationRole;
import com.nhnacademy.front.organization.dto.request.OrganizationMemberSearchRequest;
import com.nhnacademy.front.organization.dto.request.MemberDepartmentAssignRequest;
import com.nhnacademy.front.organization.dto.request.OrganizationRoleUpdateRequest;
import com.nhnacademy.front.organization.dto.response.OrgDetailResponse;
import com.nhnacademy.front.organization.dto.response.DepartmentListResponse;
import com.nhnacademy.front.organization.dto.response.OrganizationMemberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/organizations/me/members")
public class OrganizationMemberController {
    private final OrganizationMemberApiClient organizationMemberApiClient;
    private final OrganizationApiClient organizationApiClient;
    private final DepartmentApiClient departmentApiClient;
    private final MemberDepartmentApiClient memberDepartmentApiClient;

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

    /**
     * 조직원 관리 -> 부서 할당
     */
    @PostMapping("/{member-id}/departments")
    public String assignMemberDepartments(@PathVariable("member-id") Long memberId,
                                          @RequestParam(required = false) List<Long> departmentIds) {
        memberDepartmentApiClient.assignMemberDepartments(memberId, new MemberDepartmentAssignRequest(departmentIds));
        return "redirect:/organizations/me/members";
    }

    private List<DepartmentListResponse> getActiveDepartments() {
        return departmentApiClient.getDepartments().stream()
                .filter(department -> department.status().name().equals("ACTIVE"))
                .toList();
    }
}
