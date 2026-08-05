package com.nhnacademy.front.account.dto.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AccountInfoResponse(

        @Email
        @NotBlank
        String email,

        @NotBlank
        String name,

        @NotNull
        LocalDateTime createdAt
) {
}
