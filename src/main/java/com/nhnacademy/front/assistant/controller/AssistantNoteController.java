package com.nhnacademy.front.assistant.controller;

import com.nhnacademy.front.assistant.client.AssistantApiClient;
import com.nhnacademy.front.assistant.dto.AssistantNoteListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chatbot/notes")
@RequiredArgsConstructor
public class AssistantNoteController {

    private static final AssistantNoteListResponse EMPTY = new AssistantNoteListResponse(0, List.of());

    private final AssistantApiClient assistantApiClient;

    @GetMapping
    public AssistantNoteListResponse getNotes(
            @RequestParam(name = "unread", defaultValue = "true") boolean unreadOnly
    ) {
        try {
            return assistantApiClient.getNotes(unreadOnly);
        } catch (Exception e) {
            log.warn("작업 점검 알림 조회 실패: {}", e.getMessage());

            return EMPTY;
        }
    }

    @PostMapping("/{note-id}/read")
    public ResponseEntity<Void> markRead(@PathVariable("note-id") Long noteId) {
        try {
            assistantApiClient.markRead(noteId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.warn("작업 점검 알림 읽음 처리 실패. noteId={}, {}", noteId, e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}
