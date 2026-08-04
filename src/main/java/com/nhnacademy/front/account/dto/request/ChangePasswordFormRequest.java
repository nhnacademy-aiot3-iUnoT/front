package com.nhnacademy.front.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordFormRequest(
        @NotBlank(message = "기존 비밀번호를 입력해주세요.")
        @Size(min = 6, max = 64, message = "기존 비밀번호는 6자 이상 64자 이하여야 합니다.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Size(min = 6, max = 64, message = "새 비밀번호는 6자 이상 64자 이하여야 합니다.")
        String newPassword,

        @NotBlank(message = "새 비밀번호를 다시 입력해주세요.")
        @Size(min = 6, max = 64, message = "비밀번호 확인은 6자 이상 64자 이하여야 합니다.")
        String confirmPassword
) {
    public ChangePasswordFormRequest() {
        this("", "", "");
    }
}
