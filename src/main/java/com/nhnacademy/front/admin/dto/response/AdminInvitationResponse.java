package com.nhnacademy.front.admin.dto.response;

import com.nhnacademy.front.organization.dto.InvitationStatus;

import java.time.LocalDateTime;

public record AdminInvitationResponse(
        Long id,
        String email,
        InvitationStatus status,
        LocalDateTime emailSentAt,
        LocalDateTime expiredAt
) {
}
