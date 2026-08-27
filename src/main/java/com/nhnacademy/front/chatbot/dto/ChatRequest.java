package com.nhnacademy.front.chatbot.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "메시지를 입력해주세요.")
        String message
) {
}
