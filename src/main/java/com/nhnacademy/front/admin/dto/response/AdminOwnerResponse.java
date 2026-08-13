package com.nhnacademy.front.admin.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminOwnerResponse(
        UUID accountUuid,
        LocalDateTime joinedAt
) {
}
