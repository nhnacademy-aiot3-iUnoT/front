package com.nhnacademy.front.assistant.dto;

import java.util.List;

public record AssistantNoteListResponse(
        long unreadCount,
        List<AssistantNoteResponse> notes
) {
}
