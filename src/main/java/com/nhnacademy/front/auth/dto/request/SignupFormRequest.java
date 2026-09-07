package com.nhnacademy.front.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupFormRequest(
        @NotBlank(message = "초대 토큰이 필요합니다.")
        String inviteToken,

        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 254, message = "이메일은 254자 이하여야 합니다.")
        String email,

        @NotBlank(message = "이름을 입력해주세요.")
        @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
        String name,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 6, max = 64, message = "비밀번호는 6자 이상 64자 이하여야 합니다.")
        String password,

        @NotBlank(message = "비밀번호를 다시 입력해주세요.")
        @Size(min = 6, max = 64, message = "비밀번호 확인은 6자 이상 64자 이하여야 합니다.")
        String confirmPassword
) {
}
