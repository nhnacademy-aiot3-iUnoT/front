package com.nhnacademy.front.organization.dto.request;

import com.nhnacademy.front.organization.dto.DepartmentStatus;
import jakarta.validation.constraints.NotNull;

public record DepartmentStatusUpdateRequest(
        @NotNull(message = "변경할 부서 상태를 선택해주세요.")
        DepartmentStatus status
) {
}
