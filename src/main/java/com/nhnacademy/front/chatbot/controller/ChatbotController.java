package com.nhnacademy.front.chatbot.controller;

import com.nhnacademy.front.chatbot.client.ChatbotApiClient;
import com.nhnacademy.front.chatbot.dto.ChatRequest;
import com.nhnacademy.front.chatbot.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class ChatbotController {
    private final ChatbotApiClient chatbotApiClient;

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatbotApiClient.chat(request);
    }
}
