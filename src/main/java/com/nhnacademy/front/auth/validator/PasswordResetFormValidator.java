package com.nhnacademy.front.auth.validator;

import com.nhnacademy.front.auth.dto.request.ResetPasswordFormRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.springframework.util.StringUtils.hasText;

@Component
public class PasswordResetFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ResetPasswordFormRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!(target instanceof ResetPasswordFormRequest request)) {
            return;
        }

        if (hasText(request.newPassword())
                && hasText(request.confirmPassword())
                && !request.newPassword().equals(request.confirmPassword())) {
            errors.rejectValue(
                    "confirmPassword",
                    "passwordMismatch",
                    "새 비밀번호가 일치하지 않습니다."
            );
        }
    }
}
