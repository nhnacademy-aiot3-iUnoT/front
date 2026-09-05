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

function validateSignupEmail() {
    const value = emailInput.value;

    if (!isRequired(value)) {
        setError(emailInput, "이메일을 입력해주세요.");
        return false;
    }

    if (!isMaxLength(value, 254)) {
        setError(emailInput, "이메일은 254자 이하여야 합니다.");
        return false;
    }

    if (!isEmail(value)) {
        setError(emailInput, "올바른 이메일 형식이 아닙니다.");
        return false;
    }

    return true;
}

function validateSignupPassword(input, requiredMessage, lengthMessage) {
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

function validateSignupName() {
    if (!isRequired(nameInput.value)) {
        setError(nameInput, "이름을 입력해주세요.");
        return false;
    }

    if (!isMaxLength(nameInput.value, 100)) {
        setError(nameInput, "이름은 100자 이하여야 합니다.");
        return false;
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
        setError(confirmPasswordInput, "비밀번호가 일치하지 않습니다.");
        return false;
    }

    return confirmPasswordValid;
}

function setEmailInvalid(message) {
    setError(emailInput, message);
    emailInput.classList.remove("is-valid");
    verifiedEmail = null;
    submitButton.disabled = true;
}

function setEmailValid(checkedEmail) {
    clearError(emailInput, "이메일을 입력해주세요.");
    emailInput.classList.add("is-valid");
    emailInput.value = checkedEmail;
    verifiedEmail = checkedEmail;
    submitButton.disabled = false;
}

async function checkEmail() {
    const checkedEmail = emailInput.value.trim();

    clearError(emailInput, "이메일을 입력해주세요.");

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
    clearError(emailInput, "이메일을 입력해주세요.");
});

passwordInput.addEventListener("input", () => {
    clearError(passwordInput, "비밀번호는 6자 이상 64자 이하여야 합니다.");
    clearError(confirmPasswordInput, "비밀번호를 다시 입력해주세요.");
});
confirmPasswordInput.addEventListener("input", () => clearError(
    confirmPasswordInput,
    "비밀번호를 다시 입력해주세요."
));
nameInput.addEventListener("input", () => clearError(nameInput, "이름을 입력해주세요."));

signupForm.addEventListener("submit", async event => {
    event.preventDefault();
    submitButton.disabled = true;

    if (emailCheckPromise) {
        await emailCheckPromise;
    }

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
