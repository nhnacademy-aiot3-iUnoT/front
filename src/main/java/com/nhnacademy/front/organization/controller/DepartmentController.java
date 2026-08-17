package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.organization.client.DepartmentApiClient;
import com.nhnacademy.front.organization.dto.request.DepartmentCreateRequest;
import com.nhnacademy.front.organization.dto.request.DepartmentStatusUpdateRequest;
import com.nhnacademy.front.organization.dto.request.DepartmentUpdateRequest;
import com.nhnacademy.front.organization.dto.response.DepartmentInfoResponse;
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

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentApiClient departmentApiClient;

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

    @GetMapping("/{departmentId}")
    public String departmentInfo(@PathVariable Long departmentId, Model model) {
        DepartmentInfoResponse department = departmentApiClient.getDepartment(departmentId);

        model.addAttribute("department", department);
        model.addAttribute("departmentUpdateRequest", new DepartmentUpdateRequest(department.name(), department.description()));
        return "organization/department-info";
    }

    @PutMapping("/{departmentId}")
    public String updateDepartment(@PathVariable Long departmentId,
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

    @PutMapping("/{departmentId}/status")
    public String updateDepartmentStatus(@PathVariable Long departmentId,
                                         @Valid @ModelAttribute DepartmentStatusUpdateRequest request,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/departments/" + departmentId;
        }

        departmentApiClient.updateDepartmentStatus(departmentId, request);
        return "redirect:/departments/" + departmentId;
    }

    @DeleteMapping("/{departmentId}")
    public String deleteDepartment(@PathVariable Long departmentId) {
        departmentApiClient.deleteDepartment(departmentId);
        return "redirect:/departments";
    }
}
