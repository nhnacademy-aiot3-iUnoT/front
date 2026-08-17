package com.nhnacademy.front.organization.dto.request;

import com.nhnacademy.front.organization.dto.OrganizationRole;
import jakarta.validation.constraints.NotNull;

public record OrganizationRoleUpdateRequest(
        @NotNull(message = "조직원의 역할은 필수 입력입니다.")
        OrganizationRole role
) {
}
