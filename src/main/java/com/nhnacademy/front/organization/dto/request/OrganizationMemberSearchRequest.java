package com.nhnacademy.front.organization.dto.request;

import com.nhnacademy.front.organization.dto.OrganizationRole;

public record OrganizationMemberSearchRequest(
        String email,
        OrganizationRole role
) {
}
