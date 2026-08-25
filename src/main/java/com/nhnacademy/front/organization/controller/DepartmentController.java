package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.organization.client.DepartmentApiClient;
import com.nhnacademy.front.organization.service.DepartmentPageService;
import com.nhnacademy.front.organization.dto.response.*;
import com.nhnacademy.front.organization.dto.request.DepartmentCreateRequest;
import com.nhnacademy.front.organization.dto.request.DepartmentStatusUpdateRequest;
import com.nhnacademy.front.organization.dto.request.DepartmentUpdateRequest;
import com.nhnacademy.front.organization.dto.response.DepartmentListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.nhnacademy.front.organization.dto.response.DepartmentPageResponse;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentApiClient departmentApiClient;
    private final DepartmentPageService departmentPageService;

    @GetMapping
    public String departmentList(Model model) {
        List<DepartmentListResponse> departments = departmentApiClient.getDepartments();

        model.addAttribute("departments", departments);
        model.addAttribute("departmentCreateRequest", new DepartmentCreateRequest());
        return "organization/department-list";
    }

    @PostMapping
    public String createDepartment(@Valid @ModelAttribute("departmentCreateRequest") DepartmentCreateRequest request,
                                   BindingResult bindingResult,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentApiClient.getDepartments());
            return "organization/department-list";
        }

        departmentApiClient.createDepartment(request);
        return "redirect:/departments";
    }

    @GetMapping("/{department-id}")
    public String departmentInfo(@PathVariable(name = "department-id") Long departmentId,
                                 Model model) {
        DepartmentPageResponse page = departmentPageService.getDepartmentPage(departmentId);

        model.addAttribute("department", page.department());
        model.addAttribute("departmentUpdateRequest", new DepartmentUpdateRequest(page.department().name(), page.department().description()));
        model.addAttribute("memberRole", page.memberRole());

        // 부서에 속한 저장소, 조직원 목록
        model.addAttribute("departmentStorages", page.storages());
        model.addAttribute("departmentMembers", page.members());

        return "organization/department-info";
    }

    @PutMapping("/{department-id}")
    public String updateDepartment(@PathVariable(name = "department-id") Long departmentId,
                                   @Valid @ModelAttribute("departmentUpdateRequest") DepartmentUpdateRequest request,
                                   BindingResult bindingResult,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("department", departmentApiClient.getDepartment(departmentId));
            return "organization/department-info";
        }

        departmentApiClient.updateDepartment(departmentId, request);
        return "redirect:/departments/" + departmentId;
    }

    @PutMapping("/{department-id}/status")
    public String updateDepartmentStatus(@PathVariable(name = "department-id") Long departmentId,
                                         @Valid @ModelAttribute DepartmentStatusUpdateRequest request,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/departments/" + departmentId;
        }

        departmentApiClient.updateDepartmentStatus(departmentId, request);
        return "redirect:/departments/" + departmentId;
    }

    @DeleteMapping("/{department-id}")
    public String deleteDepartment(@PathVariable(name = "department-id") Long departmentId) {
        departmentApiClient.deleteDepartment(departmentId);
        return "redirect:/departments";
    }
}
