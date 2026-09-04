package com.nhnacademy.front.auth.validator;

import com.nhnacademy.front.auth.dto.request.SignupFormRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.springframework.util.StringUtils.hasText;

@Component
public class SignupFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return SignupFormRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!(target instanceof SignupFormRequest request)) {
            return;
        }

        if (hasText(request.password())
                && hasText(request.confirmPassword())
                && !request.password().equals(request.confirmPassword())) {
            errors.rejectValue(
                    "confirmPassword",
                    "passwordMismatch",
                    "비밀번호가 일치하지 않습니다."
            );
        }
    }
}
