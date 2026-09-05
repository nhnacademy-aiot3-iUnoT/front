package com.nhnacademy.front.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ReactivationConfirmRequest(
        @NotBlank(message = "인증 토큰이 필요합니다.")
        @Pattern(
                regexp = "^[0-9a-f]{64}$",
                message = "인증 링크가 올바르지 않습니다. 인증 메일을 다시 요청해주세요."
        )
        String token
) {
}
