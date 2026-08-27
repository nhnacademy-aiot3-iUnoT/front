package com.nhnacademy.front.account.dto.response;

import com.nhnacademy.front.account.dto.AccountRole;
import com.nhnacademy.front.account.dto.AccountStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID uuid,
        String name,
        String email,
        AccountRole accountRole,
        AccountStatus accountStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime withdrawnAt
) {
}
