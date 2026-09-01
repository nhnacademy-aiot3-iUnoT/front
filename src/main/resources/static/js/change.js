const changePasswordForm = document.getElementById("change-password-form");
const currentPasswordInput = document.getElementById("current-password");
const newPasswordInput = document.getElementById("new-password");
const confirmPasswordInput = document.getElementById("confirm-password");
const newPasswordFeedback = document.getElementById("new-password-feedback");
const confirmPasswordFeedback = document.getElementById("confirm-password-feedback");

if (!changePasswordForm || !currentPasswordInput || !newPasswordInput || !confirmPasswordInput
    || !newPasswordFeedback || !confirmPasswordFeedback) {
    throw new Error("비밀번호 변경 폼을 찾을 수 없습니다.");
}

function validatePasswordRelationship() {
    const currentPassword = currentPasswordInput.value;
    const newPassword = newPasswordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    newPasswordInput.setCustomValidity("");
    confirmPasswordInput.setCustomValidity("");

    if (currentPassword !== "" && currentPassword === newPassword) {
        const message = "새 비밀번호는 기존 비밀번호와 달라야 합니다.";
        newPasswordInput.setCustomValidity(message);
        setError(newPasswordInput, message);
    } else {
        newPasswordInput.classList.remove("is-invalid");
        newPasswordFeedback.classList.remove("d-block");
        newPasswordFeedback.textContent = "새 비밀번호는 6자 이상 64자 이하여야 합니다.";
    }

    if (newPassword !== "" && confirmPassword !== "" && newPassword !== confirmPassword) {
        const message = "새 비밀번호가 일치하지 않습니다.";
        confirmPasswordInput.setCustomValidity(message);
        setError(confirmPasswordInput, message);
    } else {
        confirmPasswordInput.classList.remove("is-invalid");
        confirmPasswordFeedback.classList.remove("d-block");
        confirmPasswordFeedback.textContent = "새 비밀번호를 다시 입력해주세요.";
    }
}

[currentPasswordInput, newPasswordInput, confirmPasswordInput].forEach(input => {
    input.addEventListener("input", () => {
        input.classList.remove("is-invalid");
        validatePasswordRelationship();
    });
});

changePasswordForm.addEventListener("submit", event => {
    validatePasswordRelationship();
    changePasswordForm.classList.add("was-validated");

    if (!changePasswordForm.checkValidity()) {
        event.preventDefault();
        event.stopPropagation();
    }
});
