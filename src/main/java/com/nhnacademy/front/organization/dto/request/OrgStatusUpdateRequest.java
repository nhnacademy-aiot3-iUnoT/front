package com.nhnacademy.front.organization.dto.request;

import com.nhnacademy.front.admin.dto.OrganizationStatus;
import jakarta.validation.constraints.NotNull;

public record OrgStatusUpdateRequest (
        @NotNull(message = "변경하고자 하는 조직 상태를 입력해주세요")
        OrganizationStatus status
){
}
