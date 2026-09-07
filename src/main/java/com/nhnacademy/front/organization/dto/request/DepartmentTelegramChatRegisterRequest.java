package com.nhnacademy.front.organization.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentTelegramChatRegisterRequest(
        @NotBlank(message = "단톡방 번호를 입력해주세요.")
        @Size(max = 255, message = "단톡방 번호는 255자를 넘을 수 없습니다.")
        String chatId,

        Boolean enabled
) {
}
