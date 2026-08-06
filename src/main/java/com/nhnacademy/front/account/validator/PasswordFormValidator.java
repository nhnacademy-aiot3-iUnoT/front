package com.nhnacademy.front.account.validator;

import com.nhnacademy.front.account.dto.request.ChangePasswordFormRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.springframework.util.StringUtils.hasText;

@Component
public class PasswordFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ChangePasswordFormRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!(target instanceof ChangePasswordFormRequest request)) {
            return;
        }

        validateConfirmation(
                request.newPassword(),
                request.confirmPassword(),
                errors
        );

        validateDifferentFromCurrent(request, errors);
    }

    private void validateConfirmation(
            String newPassword,
            String confirmPassword,
            Errors errors) {
        if (hasText(newPassword)
                && hasText(confirmPassword)
                && !newPassword.equals(confirmPassword)) {

            errors.rejectValue(
                    "confirmPassword",
                    "passwordMismatch",
                    "새 비밀번호가 일치하지 않습니다."
            );
        }
    }

    private void validateDifferentFromCurrent(
            ChangePasswordFormRequest request,
            Errors errors)
    {
        if (hasText(request.currentPassword())
                && hasText(request.newPassword())
                && request.currentPassword().equals(request.newPassword())) {

            errors.rejectValue(
                    "newPassword",
                    "sameAsCurrentPassword",
                    "새 비밀번호는 기존 비밀번호와 달라야 합니다."
            );
        }
    }
}
