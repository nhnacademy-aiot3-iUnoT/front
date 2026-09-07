package com.nhnacademy.front.assistant.client;

import com.nhnacademy.front.assistant.dto.AssistantNoteListResponse;
import com.nhnacademy.front.global.client.GatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssistantApiClient {

    private static final String NOTES_PATH = "/api/core/assistant/notes";

    private final GatewayClient gatewayClient;

    public AssistantNoteListResponse getNotes(boolean unreadOnly) {
        return gatewayClient.get(NOTES_PATH + "?unread=" + unreadOnly,
                new ParameterizedTypeReference<>() {});
    }

    public void markRead(Long noteId) {
        gatewayClient.post(NOTES_PATH + "/" + noteId + "/read");
    }
}
