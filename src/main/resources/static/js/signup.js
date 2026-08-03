const pwdInput = document.getElementById("pwd");
const pwdCheckInput = document.getElementById("pwd-check");
const emailInput = document.getElementById("email");
const emailCheckButton = document.getElementById("emailCheck");
const emailFeedback = document.getElementById("email-feedback");
const signupForm = document.getElementById("signup-form");
const submitButton = document.getElementById("signup-submit");
let verifiedEmail = null;
let emailCheckPromise = null;

function checkPwdEquals() {
    const isEqual = pwdInput.value === pwdCheckInput.value;

    pwdCheckInput.classList.toggle(
        "is-invalid",
        !isEqual && pwdInput.value !== "" && pwdCheckInput.value !== ""
    );

    pwdCheckInput.setCustomValidity(
        isEqual ? "" : "비밀번호가 일치하지 않습니다."
    );

    return isEqual;
}

pwdInput.addEventListener("input", checkPwdEquals);
pwdCheckInput.addEventListener("input", checkPwdEquals);


function setEmailInvalid(message) {
    emailInput.classList.add("is-invalid");
    emailInput.classList.remove("is-valid");
    emailInput.setCustomValidity(message);
    emailFeedback.textContent = message;
    verifiedEmail = null;
    submitButton.disabled = true;
}

function setEmailValid(checkedEmail) {
    emailInput.classList.remove("is-invalid");
    emailInput.classList.add("is-valid");
    emailInput.setCustomValidity("");
    verifiedEmail = checkedEmail;
    submitButton.disabled = false;
}

async function checkEmail() {
    const checkedEmail = emailInput.value.trim();

    emailInput.setCustomValidity("");

    if (!emailInput.validity.valid) {
        const message = emailInput.validity.valueMissing
            ? "이메일을 입력해주세요."
            : "올바른 이메일 형식으로 입력해주세요.";

        setEmailInvalid(message);
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
    emailInput.classList.remove("is-valid", "is-invalid");
    emailInput.setCustomValidity("");
});

signupForm.addEventListener("submit", async event => {
    event.preventDefault();
    submitButton.disabled = true;

    if (emailCheckPromise) {
        await emailCheckPromise;
    }

    if (verifiedEmail !== emailInput.value.trim()) {
        setEmailInvalid("이메일 중복 확인이 필요합니다.");
        emailInput.reportValidity();
        return;
    }

    checkPwdEquals();

    if (!signupForm.reportValidity()) {
        submitButton.disabled = false;
        return;
    }

    signupForm.submit();
});