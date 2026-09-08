function validateAuthEmail(input) {
    const value = input.value;

    if (!isRequired(value)) {
        setError(input, "이메일을 입력해주세요.");
        return false;
    }

    if (!isMaxLength(value, 254)) {
        setError(input, "이메일은 254자 이하여야 합니다.");
        return false;
    }

    if (!isEmail(value)) {
        setError(input, "올바른 이메일 형식이 아닙니다.");
        return false;
    }

    return true;
}

function validateAuthPassword(input, requiredMessage, lengthMessage) {
    const value = input.value;

    if (!isRequired(value)) {
        setError(input, requiredMessage);
        return false;
    }

    if (!isLengthInRange(value, 6, 64)) {
        setError(input, lengthMessage);
        return false;
    }

    return true;
}

const loginForm = document.getElementById("login-form");

if (loginForm) {
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");
    const passwordToggle = document.getElementById("password-toggle");
    const passwordTooltip = bootstrap.Tooltip.getOrCreateInstance(passwordToggle);

    passwordToggle.addEventListener("click", () => {
        const isPasswordVisible = passwordInput.type === "text";
        const tooltipText = isPasswordVisible ? "비밀번호 보기" : "비밀번호 숨기기";

        passwordInput.type = isPasswordVisible ? "password" : "text";
        passwordToggle.setAttribute("aria-label", tooltipText);
        passwordToggle.setAttribute("aria-pressed", String(!isPasswordVisible));
        passwordToggle.setAttribute("data-bs-title", tooltipText);
        passwordTooltip.setContent({".tooltip-inner": tooltipText});

        passwordToggle.innerHTML = isPasswordVisible
            ? `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon icon-tabler icons-tabler-outline icon-tabler-eye-closed" aria-hidden="true"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M21 9c-2.4 2.667 -5.4 4 -9 4c-3.6 0 -6.6 -1.333 -9 -4"/><path d="M3 15l2.5 -3.8"/><path d="M21 14.976l-2.492 -3.776"/><path d="M9 17l.5 -4"/><path d="M15 17l-.5 -4"/></svg>`
            : `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon icon-tabler icons-tabler-outline icon-tabler-eye" aria-hidden="true"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M10 12a2 2 0 1 0 4 0a2 2 0 0 0 -4 0"/><path d="M21 12c-2.4 4 -5.4 6 -9 6c-3.6 0 -6.6 -2 -9 -6c2.4 -4 5.4 -6 9 -6c3.6 0 6.6 2 9 6"/></svg>`;
    });

    emailInput.addEventListener("input", () => clearError(emailInput, "이메일을 입력해주세요."));
    passwordInput.addEventListener("input", () => clearError(
        passwordInput,
        "비밀번호는 6자 이상 64자 이하여야 합니다."
    ));

    loginForm.addEventListener("submit", event => {
        clearErrors(loginForm);

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

    emailInput.addEventListener("input", () => clearError(emailInput, "이메일을 입력해주세요."));

    forgotPasswordForm.addEventListener("submit", event => {
        clearError(emailInput);
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

    newPasswordInput.addEventListener("input", () => clearError(
        newPasswordInput,
        "새 비밀번호는 6자 이상 64자 이하여야 합니다."
    ));
    confirmPasswordInput.addEventListener("input", () => clearError(
        confirmPasswordInput,
        "새 비밀번호를 다시 입력해주세요."
    ));

    resetPasswordForm.addEventListener("submit", event => {
        clearErrors(resetPasswordForm);

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
            setError(
                confirmPasswordInput,
                "새 비밀번호가 일치하지 않습니다."
            );
            passwordsMatch = false;
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
