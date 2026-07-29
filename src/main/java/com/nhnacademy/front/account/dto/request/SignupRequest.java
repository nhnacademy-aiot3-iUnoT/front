package com.nhnacademy.front.account.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(

        @NotBlank
        String inviteToken,

        @Email
        @NotBlank
        String email,

        @NotBlank
        String name,

        @NotBlank
        String password
) {
}
