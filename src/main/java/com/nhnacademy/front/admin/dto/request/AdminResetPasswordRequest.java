package com.nhnacademy.front.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminResetPasswordRequest(
        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Size(min = 6, max = 64, message = "새 비밀번호는 6자 이상 64자 이하여야 합니다.")
        String password
) {
}
