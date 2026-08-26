package com.nhnacademy.front.chatbot.controller;

import com.nhnacademy.front.chatbot.client.ChatbotApiClient;
import com.nhnacademy.front.chatbot.dto.ChatRequest;
import com.nhnacademy.front.chatbot.dto.ChatResponse;
import com.nhnacademy.front.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/core/chatbot")
@RequiredArgsConstructor
public class ChatbotController {
    private final ChatbotApiClient chatbotApiClient;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatbotApiClient.chat(request.message());
        return ResponseEntity.ok(new ApiResponse<>(true, response, null, LocalDateTime.now()));
    }
}
