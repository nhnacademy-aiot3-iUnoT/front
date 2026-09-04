package com.nhnacademy.front.auth.validator;

import com.nhnacademy.front.auth.dto.request.SignupFormRequest;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.assertj.core.api.Assertions.assertThat;

class SignupFormValidatorTest {

    private final SignupFormValidator validator = new SignupFormValidator();

    @Test
    void rejectsMismatchedPasswords() {
        SignupFormRequest request = new SignupFormRequest(
                "invite-token",
                "member@test.com",
                "member",
                "password",
                "different"
        );
        Errors errors = new BeanPropertyBindingResult(request, "signupRequest");

        validator.validate(request, errors);

        assertThat(errors.getFieldError("confirmPassword"))
                .isNotNull()
                .extracting(error -> error.getCode())
                .isEqualTo("passwordMismatch");
    }

    @Test
    void acceptsMatchingPasswords() {
        SignupFormRequest request = new SignupFormRequest(
                "invite-token",
                "member@test.com",
                "member",
                "password",
                "password"
        );
        Errors errors = new BeanPropertyBindingResult(request, "signupRequest");

        validator.validate(request, errors);

        assertThat(errors.hasErrors()).isFalse();
    }
}
