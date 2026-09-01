const accountInfoForm = document.getElementById("account-info-form");
const accountNameInput = document.getElementById("name");

if (!accountInfoForm || !accountNameInput) {
    throw new Error("회원정보 수정 폼을 찾을 수 없습니다.");
}

function validateAccountName() {
    const value = accountNameInput.value;
    let message = "";

    clearErrors(accountInfoForm);
    accountNameInput.setCustomValidity("");

    if (!isRequired(value)) {
        message = "이름을 입력해주세요.";
    } else if (!isMaxLength(value, 100)) {
        message = "이름은 100자 이내로 입력해주세요.";
    }

    if (message !== "") {
        accountNameInput.setCustomValidity(message);
        setError(accountNameInput, message);
        return false;
    }

    return true;
}

accountNameInput.addEventListener("input", () => {
    accountNameInput.setCustomValidity("");
    accountNameInput.classList.remove("is-invalid");
});

accountInfoForm.addEventListener("submit", event => {
    accountInfoForm.classList.add("was-validated");

    if (!validateAccountName() || !accountInfoForm.checkValidity()) {
        event.preventDefault();
        event.stopPropagation();
        accountNameInput.reportValidity();
    }
});
