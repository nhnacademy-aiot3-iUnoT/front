package com.nhnacademy.front.chatbot.client;

import com.nhnacademy.front.chatbot.dto.ChatRequest;
import com.nhnacademy.front.chatbot.dto.ChatResponse;
import com.nhnacademy.front.global.client.GatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatbotApiClient {
    private static final String CHATBOT_PATH = "/api/core/chatbot";
    private final GatewayClient gatewayClient;

    public ChatResponse chat(ChatRequest request) {
        return gatewayClient.post(CHATBOT_PATH, request, new ParameterizedTypeReference<>() {});
    }
}
