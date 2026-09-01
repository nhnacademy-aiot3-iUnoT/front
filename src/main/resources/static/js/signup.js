const signupForm = document.getElementById("signup-form");
const emailInput = document.getElementById("email");
const emailCheckButton = document.getElementById("emailCheck");
const passwordInput = document.getElementById("pwd");
const confirmPasswordInput = document.getElementById("pwd-check");
const nameInput = document.getElementById("name");
const submitButton = document.getElementById("signup-submit");
let verifiedEmail = null;
let emailCheckPromise = null;

if (!signupForm
    || !emailInput
    || !emailCheckButton
    || !passwordInput
    || !confirmPasswordInput
    || !nameInput
    || !submitButton) {
    throw new Error("회원가입 폼을 찾을 수 없습니다.");
}

function resetSignupField(input, defaultMessage) {
    input.setCustomValidity("");
    input.classList.remove("is-invalid");

    const feedback = input.closest(".mb-3")?.querySelector(".invalid-feedback");
    if (feedback) {
        feedback.classList.remove("d-block");
        feedback.textContent = defaultMessage;
    }
}

function invalidateSignupField(input, message) {
    input.setCustomValidity(message);
    setError(input, message);
    return false;
}

function validateSignupEmail() {
    const value = emailInput.value;

    if (!isRequired(value)) {
        return invalidateSignupField(emailInput, "이메일을 입력해주세요.");
    }

    if (!isMaxLength(value, 254)) {
        return invalidateSignupField(emailInput, "이메일은 254자 이하여야 합니다.");
    }

    if (!isEmail(value)) {
        return invalidateSignupField(emailInput, "올바른 이메일 형식이 아닙니다.");
    }

    return true;
}

function validateSignupPassword(input, requiredMessage, lengthMessage) {
    if (!isRequired(input.value)) {
        return invalidateSignupField(input, requiredMessage);
    }

    if (input.value.length < 6 || !isMaxLength(input.value, 64)) {
        return invalidateSignupField(input, lengthMessage);
    }

    return true;
}

function validateSignupName() {
    if (!isRequired(nameInput.value)) {
        return invalidateSignupField(nameInput, "이름을 입력해주세요.");
    }

    if (!isMaxLength(nameInput.value, 100)) {
        return invalidateSignupField(nameInput, "이름은 100자 이하여야 합니다.");
    }

    return true;
}

function validatePasswordConfirmation() {
    const confirmPasswordValid = validateSignupPassword(
        confirmPasswordInput,
        "비밀번호를 다시 입력해주세요.",
        "비밀번호 확인은 6자 이상 64자 이하여야 합니다."
    );

    if (confirmPasswordValid && passwordInput.value !== confirmPasswordInput.value) {
        return invalidateSignupField(confirmPasswordInput, "비밀번호가 일치하지 않습니다.");
    }

    return confirmPasswordValid;
}

function setEmailInvalid(message) {
    invalidateSignupField(emailInput, message);
    emailInput.classList.remove("is-valid");
    verifiedEmail = null;
    submitButton.disabled = true;
}

function setEmailValid(checkedEmail) {
    resetSignupField(emailInput, "이메일을 입력해주세요.");
    emailInput.classList.add("is-valid");
    emailInput.value = checkedEmail;
    verifiedEmail = checkedEmail;
    submitButton.disabled = false;
}

async function checkEmail() {
    const checkedEmail = emailInput.value.trim();

    resetSignupField(emailInput, "이메일을 입력해주세요.");

    if (!validateSignupEmail()) {
        return false;
    }

    try {
        const response = await fetch("/check-email", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email: checkedEmail
            })
        });

        if (!response.ok) {
            throw new Error("이메일 중복 확인 요청 실패");
        }

        const data = await response.json();

        if (emailInput.value.trim() !== checkedEmail) {
            return false;
        }

        if (!data.available) {
            setEmailInvalid("이미 사용 중인 이메일입니다.");
            return false;
        }

        setEmailValid(checkedEmail);
        return true;
    } catch (error) {
        setEmailInvalid("이메일 중복 확인 중 오류가 발생했습니다.");
        return false;
    }
}

async function startEmailCheck() {
    if (emailCheckPromise) {
        return emailCheckPromise;
    }

    verifiedEmail = null;
    submitButton.disabled = true;
    emailCheckButton.disabled = true;

    const promise = checkEmail();
    emailCheckPromise = promise;

    try {
        return await promise;
    } finally {
        if (emailCheckPromise === promise) {
            emailCheckPromise = null;
        }
        emailCheckButton.disabled = false;
    }
}

emailCheckButton.addEventListener("click", startEmailCheck);

emailInput.addEventListener("input", () => {
    verifiedEmail = null;
    submitButton.disabled = true;
    emailInput.classList.remove("is-valid");
    resetSignupField(emailInput, "이메일을 입력해주세요.");
});

passwordInput.addEventListener("input", () => {
    resetSignupField(passwordInput, "비밀번호는 6자 이상 64자 이하여야 합니다.");
    resetSignupField(confirmPasswordInput, "비밀번호를 다시 입력해주세요.");
});
confirmPasswordInput.addEventListener("input", () => resetSignupField(
    confirmPasswordInput,
    "비밀번호를 다시 입력해주세요."
));
nameInput.addEventListener("input", () => resetSignupField(nameInput, "이름을 입력해주세요."));

signupForm.addEventListener("submit", async event => {
    event.preventDefault();
    submitButton.disabled = true;

    if (emailCheckPromise) {
        await emailCheckPromise;
    }

    passwordInput.setCustomValidity("");
    confirmPasswordInput.setCustomValidity("");
    nameInput.setCustomValidity("");

    const emailValid = validateSignupEmail();
    let valid = emailValid;

    if (emailValid && verifiedEmail !== emailInput.value.trim()) {
        setEmailInvalid("이메일 중복 확인이 필요합니다.");
        valid = false;
    }

    valid = validateSignupPassword(
        passwordInput,
        "비밀번호를 입력해주세요.",
        "비밀번호는 6자 이상 64자 이하여야 합니다."
    ) && valid;
    valid = validatePasswordConfirmation() && valid;
    valid = validateSignupName() && valid;
    signupForm.classList.add("was-validated");

    if (!valid || !signupForm.reportValidity()) {
        submitButton.disabled = verifiedEmail === null;
        return;
    }

    signupForm.submit();
});
