package com.nhnacademy.front.account.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CheckEmailRequest(
        @NotBlank
        @Email
        String email
) {
}
