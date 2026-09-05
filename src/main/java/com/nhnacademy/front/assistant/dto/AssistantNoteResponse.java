package com.nhnacademy.front.assistant.dto;

import java.time.LocalDateTime;

public record AssistantNoteResponse(
        Long noteId,
        String operation,
        String severity,
        String findingType,
        String subject,
        String subjectDetail,
        String message,
        String targetType,
        Long targetStorageId,
        Long targetId,
        boolean read,
        LocalDateTime createdAt
) {
}
