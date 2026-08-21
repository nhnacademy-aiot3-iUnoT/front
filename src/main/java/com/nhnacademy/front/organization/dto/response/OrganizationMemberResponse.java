package com.nhnacademy.front.organization.dto.response;

import com.nhnacademy.front.organization.dto.OrganizationRole;

import java.time.LocalDateTime;

public record OrganizationMemberResponse(
        Long memberId,
        String email,
        OrganizationRole role,
        LocalDateTime joinedAt
) {
}
