// JS 검증 오류의 표시와 초기화를 담당한다.
function setError(input, message) {
    if (!input) {
        return;
    }

    input.classList.add("is-invalid");

    const fieldGroup = input.closest(".mb-3, .mb-4");
    const feedback = fieldGroup?.querySelector(".invalid-feedback");

    if (!feedback) {
        return;
    }

    feedback.textContent = message;
    feedback.classList.add("d-block");
}

function clearError(input, defaultMessage = "") {
    if (!input) {
        return;
    }

    input.classList.remove("is-invalid");

    const fieldGroup = input.closest(".mb-3, .mb-4");
    const feedback = fieldGroup?.querySelector(".invalid-feedback");

    if (!feedback) {
        return;
    }

    feedback.textContent = defaultMessage;
    feedback.classList.remove("d-block");
}

function clearErrors(container) {
    if (!container) {
        return;
    }

    container.querySelectorAll(".is-invalid")
        .forEach(input => clearError(input));
}
