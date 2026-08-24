package com.nhnacademy.front.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAccountNameRequest(
        @NotBlank(message = "이름을 입력해주세요.")
        @Size(max = 100, message = "이름은 100자 이내로 입력해주세요.")
        String name
) {
}
