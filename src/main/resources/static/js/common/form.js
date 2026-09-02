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

function clearFieldError(input) {
    if (!input) {
        return;
    }

    input.classList.remove("is-invalid");

    const fieldGroup = input.closest(".mb-3, .mb-4");
    const feedback = fieldGroup?.querySelector(".invalid-feedback");

    if (feedback) {
        feedback.textContent = "";
        feedback.classList.remove("d-block");
    }
}

function clearErrors(form) {
    if (!form) {
        return;
    }

    form.querySelectorAll(".form-control, .form-select")
        .forEach(input => input.classList.remove("is-invalid"));

    form.querySelectorAll(".invalid-feedback")
        .forEach(feedback => {
            feedback.textContent = "";
            feedback.classList.remove("d-block");
        });
}
