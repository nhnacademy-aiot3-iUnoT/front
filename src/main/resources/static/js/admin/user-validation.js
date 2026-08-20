document.addEventListener("DOMContentLoaded", function () {
    configureAccountForm("#adminUserCreateForm", [
        {selector: "#name", event: "input", validate: validateAccountName},
        {selector: "#email", event: "input", validate: validateAccountEmail},
        {
            selector: "#password",
            event: "input",
            validate: input => validateAccountPassword(input, "비밀번호")
        }
    ]);

    configureAccountForm("#adminUserNameForm", [
        {selector: "#name", event: "input", validate: validateAccountName}
    ]);

    configureAccountForm("#adminUserPasswordForm", [
        {
            selector: "#password",
            event: "input",
            validate: input => validateAccountPassword(input, "새 비밀번호")
        }
    ]);

    configureAccountForm("#adminUserStatusForm", [
        {selector: "#action", event: "change", validate: validateAccountStatusAction},
        {selector: "#reason", event: "input", validate: validateAccountStatusReason}
    ]);
});

function configureAccountForm(formSelector, fieldDefinitions) {
    const form = document.querySelector(formSelector);

    if (!form) {
        return;
    }

    const fields = fieldDefinitions.map(definition => ({
        input: form.querySelector(definition.selector),
        event: definition.event,
        validate: definition.validate
    }));

    if (fields.some(field => !field.input)) {
        return;
    }

    fields.forEach(field => {
        field.input.addEventListener("blur", function () {
            clearAccountError(field.input);
            field.validate(field.input);
        });

        field.input.addEventListener(field.event, function () {
            if (field.input.classList.contains("is-invalid")) {
                clearAccountError(field.input);
                field.validate(field.input);
            }
        });
    });

    form.addEventListener("submit", function (event) {
        fields.forEach(field => clearAccountError(field.input));

        const valid = fields
            .map(field => field.validate(field.input))
            .every(Boolean);

        if (!valid) {
            event.preventDefault();
            focusFirstInvalidAccountField(form);
        }
    });
}

function validateAccountName(input) {
    const value = input.value;

    if (!isAccountRequired(value)) {
        setAccountError(input, "이름을 입력해주세요.");
        return false;
    }

    if (value.length > 100) {
        setAccountError(input, "이름은 100자 이내로 입력해주세요.");
        return false;
    }

    return true;
}

function validateAccountEmail(input) {
    const value = input.value;

    if (!isAccountRequired(value)) {
        setAccountError(input, "이메일을 입력해주세요.");
        return false;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        setAccountError(input, "올바른 이메일 형식이 아닙니다.");
        return false;
    }

    if (value.length > 254) {
        setAccountError(input, "이메일은 254자 이내로 입력해주세요.");
        return false;
    }

    return true;
}

function validateAccountPassword(input, label) {
    const value = input.value;

    if (!isAccountRequired(value)) {
        setAccountError(input, `${label}를 입력해주세요.`);
        return false;
    }

    if (value.length < 6 || value.length > 64) {
        setAccountError(input, `${label}는 6자 이상 64자 이하여야 합니다.`);
        return false;
    }

    return true;
}

function validateAccountStatusAction(input) {
    if (!isAccountRequired(input.value)) {
        setAccountError(input, "변경할 상태를 선택해주세요.");
        return false;
    }

    return true;
}

function validateAccountStatusReason(input) {
    const value = input.value;

    if (!isAccountRequired(value)) {
        setAccountError(input, "상태 변경 사유를 입력해주세요.");
        return false;
    }

    if (value.length > 200) {
        setAccountError(input, "변경 사유는 200자 이내로 입력해주세요.");
        return false;
    }

    return true;
}

function isAccountRequired(value) {
    return value !== null && value.trim().length > 0;
}

function setAccountError(input, message) {
    input.classList.add("is-invalid");
    input.setAttribute("aria-invalid", "true");

    const feedback = findAccountFeedback(input);

    if (feedback) {
        feedback.textContent = message;
    }
}

function clearAccountError(input) {
    input.classList.remove("is-invalid");
    input.removeAttribute("aria-invalid");

    const feedback = findAccountFeedback(input);

    if (feedback) {
        feedback.textContent = "";
    }
}

function findAccountFeedback(input) {
    const field = input.closest("[data-account-validation-field]");
    return field ? field.querySelector(".invalid-feedback") : null;
}

function focusFirstInvalidAccountField(form) {
    const firstInvalid = form.querySelector(".is-invalid");

    if (firstInvalid) {
        firstInvalid.focus();
    }
}
