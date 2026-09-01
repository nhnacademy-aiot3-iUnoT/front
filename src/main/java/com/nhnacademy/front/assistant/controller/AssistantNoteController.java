package com.nhnacademy.front.assistant.controller;

import com.nhnacademy.front.assistant.client.AssistantApiClient;
import com.nhnacademy.front.assistant.dto.AssistantNoteListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot/notes")
@RequiredArgsConstructor
public class AssistantNoteController {

    private final AssistantApiClient assistantApiClient;

    @GetMapping
    public AssistantNoteListResponse getNotes(
            @RequestParam(name = "unread", defaultValue = "true") boolean unreadOnly
    ) {
        return assistantApiClient.getNotes(unreadOnly);
    }

    @PostMapping("/{note-id}/read")
    public void markRead(@PathVariable("note-id") Long noteId) {
        assistantApiClient.markRead(noteId);
    }
}
