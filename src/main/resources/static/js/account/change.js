const changePasswordForm = document.getElementById("change-password-form");
const currentPasswordInput = document.getElementById("current-password");
const newPasswordInput = document.getElementById("new-password");
const confirmPasswordInput = document.getElementById("confirm-password");

if (!changePasswordForm || !currentPasswordInput || !newPasswordInput || !confirmPasswordInput) {
    throw new Error("비밀번호 변경 폼을 찾을 수 없습니다.");
}

function validatePassword(input, requiredMessage, lengthMessage) {
    if (!isRequired(input.value)) {
        setError(input, requiredMessage);
        return false;
    }

    if (!isLengthInRange(input.value, 6, 64)) {
        setError(input, lengthMessage);
        return false;
    }

    return true;
}

function validateChangePasswordForm() {
    clearErrors(changePasswordForm);

    const currentPasswordValid = validatePassword(
        currentPasswordInput,
        "기존 비밀번호를 입력해주세요.",
        "기존 비밀번호는 6자 이상 64자 이하여야 합니다."
    );
    const newPasswordValid = validatePassword(
        newPasswordInput,
        "새 비밀번호를 입력해주세요.",
        "새 비밀번호는 6자 이상 64자 이하여야 합니다."
    );
    const confirmPasswordValid = validatePassword(
        confirmPasswordInput,
        "새 비밀번호를 다시 입력해주세요.",
        "비밀번호 확인은 6자 이상 64자 이하여야 합니다."
    );
    let relationshipsValid = true;

    if (currentPasswordValid
        && newPasswordValid
        && currentPasswordInput.value === newPasswordInput.value) {
        setError(
            newPasswordInput,
            "새 비밀번호는 기존 비밀번호와 달라야 합니다."
        );
        relationshipsValid = false;
    }

    if (newPasswordValid
        && confirmPasswordValid
        && newPasswordInput.value !== confirmPasswordInput.value) {
        setError(
            confirmPasswordInput,
            "새 비밀번호가 일치하지 않습니다."
        );
        relationshipsValid = false;
    }

    return currentPasswordValid
        && newPasswordValid
        && confirmPasswordValid
        && relationshipsValid;
}

[
    [currentPasswordInput, "기존 비밀번호를 입력해주세요."],
    [newPasswordInput, "새 비밀번호는 6자 이상 64자 이하여야 합니다."],
    [confirmPasswordInput, "새 비밀번호를 다시 입력해주세요."]
].forEach(([input, defaultMessage]) => {
    input.addEventListener("input", () => clearError(input, defaultMessage));
});

changePasswordForm.addEventListener("submit", event => {
    changePasswordForm.classList.add("was-validated");

    if (!validateChangePasswordForm() || !changePasswordForm.checkValidity()) {
        event.preventDefault();
        event.stopPropagation();
        changePasswordForm.reportValidity();
    }
});
