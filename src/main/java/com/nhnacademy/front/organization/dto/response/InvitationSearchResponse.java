package com.nhnacademy.front.organization.dto.response;

import com.nhnacademy.front.organization.dto.InvitationStatus;

import java.time.LocalDateTime;

public record InvitationSearchResponse(
        Long id,
        String email,
        InvitationStatus status,
        LocalDateTime createdAt,
        LocalDateTime expiredAt
) {
}
