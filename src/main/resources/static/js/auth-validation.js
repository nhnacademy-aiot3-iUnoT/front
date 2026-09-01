function resetAuthField(input, defaultMessage) {
    input.setCustomValidity("");
    input.classList.remove("is-invalid");

    const feedback = input.closest(".mb-3, .mb-4")?.querySelector(".invalid-feedback");
    if (feedback) {
        feedback.classList.remove("d-block");
        feedback.textContent = defaultMessage;
    }
}

function invalidateAuthField(input, message) {
    input.setCustomValidity(message);
    setError(input, message);
    return false;
}

function validateAuthEmail(input) {
    const value = input.value;

    if (!isRequired(value)) {
        return invalidateAuthField(input, "이메일을 입력해주세요.");
    }

    if (!isMaxLength(value, 254)) {
        return invalidateAuthField(input, "이메일은 254자 이하여야 합니다.");
    }

    if (!isEmail(value)) {
        return invalidateAuthField(input, "올바른 이메일 형식이 아닙니다.");
    }

    return true;
}

function validateAuthPassword(input, requiredMessage, lengthMessage) {
    const value = input.value;

    if (!isRequired(value)) {
        return invalidateAuthField(input, requiredMessage);
    }

    if (value.length < 6 || !isMaxLength(value, 64)) {
        return invalidateAuthField(input, lengthMessage);
    }

    return true;
}

const loginForm = document.getElementById("login-form");

if (loginForm) {
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");

    emailInput.addEventListener("input", () => resetAuthField(emailInput, "이메일을 입력해주세요."));
    passwordInput.addEventListener("input", () => resetAuthField(
        passwordInput,
        "비밀번호는 6자 이상 64자 이하여야 합니다."
    ));

    loginForm.addEventListener("submit", event => {
        clearErrors(loginForm);
        emailInput.setCustomValidity("");
        passwordInput.setCustomValidity("");

        const emailValid = validateAuthEmail(emailInput);
        const passwordValid = validateAuthPassword(
            passwordInput,
            "비밀번호를 입력해주세요.",
            "비밀번호는 6자 이상 64자 이하여야 합니다."
        );

        loginForm.classList.add("was-validated");

        if (!emailValid || !passwordValid || !loginForm.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
            loginForm.reportValidity();
        }
    });
}

const forgotPasswordForm = document.getElementById("forgot-password-form");

if (forgotPasswordForm) {
    const emailInput = document.getElementById("email");

    emailInput.addEventListener("input", () => resetAuthField(emailInput, "이메일을 입력해주세요."));

    forgotPasswordForm.addEventListener("submit", event => {
        clearErrors(forgotPasswordForm);
        emailInput.setCustomValidity("");
        forgotPasswordForm.classList.add("was-validated");

        if (!validateAuthEmail(emailInput) || !forgotPasswordForm.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
            emailInput.reportValidity();
        }
    });
}

const resetPasswordForm = document.getElementById("reset-password-form");

if (resetPasswordForm) {
    const newPasswordInput = document.getElementById("new-password");
    const confirmPasswordInput = document.getElementById("confirm-password");

    newPasswordInput.addEventListener("input", () => resetAuthField(
        newPasswordInput,
        "새 비밀번호는 6자 이상 64자 이하여야 합니다."
    ));
    confirmPasswordInput.addEventListener("input", () => resetAuthField(
        confirmPasswordInput,
        "새 비밀번호를 다시 입력해주세요."
    ));

    resetPasswordForm.addEventListener("submit", event => {
        clearErrors(resetPasswordForm);
        newPasswordInput.setCustomValidity("");
        confirmPasswordInput.setCustomValidity("");

        const newPasswordValid = validateAuthPassword(
            newPasswordInput,
            "새 비밀번호를 입력해주세요.",
            "새 비밀번호는 6자 이상 64자 이하여야 합니다."
        );
        const confirmPasswordValid = validateAuthPassword(
            confirmPasswordInput,
            "새 비밀번호를 다시 입력해주세요.",
            "비밀번호 확인은 6자 이상 64자 이하여야 합니다."
        );
        let passwordsMatch = true;

        if (newPasswordValid
            && confirmPasswordValid
            && newPasswordInput.value !== confirmPasswordInput.value) {
            passwordsMatch = invalidateAuthField(
                confirmPasswordInput,
                "새 비밀번호가 일치하지 않습니다."
            );
        }

        resetPasswordForm.classList.add("was-validated");

        if (!newPasswordValid
            || !confirmPasswordValid
            || !passwordsMatch
            || !resetPasswordForm.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
            resetPasswordForm.reportValidity();
        }
    });
}
