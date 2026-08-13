const changePasswordForm = document.getElementById("change-password-form");
const currentPasswordInput = document.getElementById("current-password");
const newPasswordInput = document.getElementById("new-password");
const confirmPasswordInput = document.getElementById("confirm-password");
const newPasswordFeedback = document.getElementById("new-password-feedback");
const confirmPasswordFeedback = document.getElementById("confirm-password-feedback");

function validatePasswordRelationship() {
    const currentPassword = currentPasswordInput.value;
    const newPassword = newPasswordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    newPasswordInput.setCustomValidity("");
    confirmPasswordInput.setCustomValidity("");

    if (currentPassword !== "" && currentPassword === newPassword) {
        const message = "새 비밀번호는 기존 비밀번호와 달라야 합니다.";
        newPasswordInput.setCustomValidity(message);
        newPasswordFeedback.textContent = message;
    } else {
        newPasswordFeedback.textContent = "새 비밀번호는 6자 이상 64자 이하여야 합니다.";
    }

    if (newPassword !== "" && confirmPassword !== "" && newPassword !== confirmPassword) {
        const message = "새 비밀번호가 일치하지 않습니다.";
        confirmPasswordInput.setCustomValidity(message);
        confirmPasswordFeedback.textContent = message;
    } else {
        confirmPasswordFeedback.textContent = "새 비밀번호를 다시 입력해주세요.";
    }

    newPasswordInput.classList.toggle("is-invalid", newPasswordInput.validity.customError);
    confirmPasswordInput.classList.toggle("is-invalid", confirmPasswordInput.validity.customError);
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
