package com.nhnacademy.front.organization.dto.response;

public record DepartmentTelegramChatResponse(
        Long departmentTelegramChatId,
        Long departmentId,
        String departmentName,
        String chatId,
        boolean enabled
) {
}
