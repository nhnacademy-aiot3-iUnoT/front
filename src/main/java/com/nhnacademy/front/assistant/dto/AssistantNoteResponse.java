package com.nhnacademy.front.assistant.dto;

import java.time.LocalDateTime;

public record AssistantNoteResponse(
        Long noteId,
        String severity,
        String message,
        String targetType,
        Long targetId,
        boolean read,
        LocalDateTime createdAt
) {
}
