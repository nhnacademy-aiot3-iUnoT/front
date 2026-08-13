package com.nhnacademy.front.organization.dto.request;

import com.nhnacademy.front.organization.dto.InvitationStatus;

public record InvitationSearchRequest(
        String email,
        InvitationStatus status
) {
}
