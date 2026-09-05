const withdrawForm = document.getElementById("withdraw-form");
const passwordInput = withdrawForm?.querySelector('input[name="password"]');
const withdrawModalElement = document.getElementById("withdrawModal");
const confirmButton = document.getElementById("withdraw-confirm-btn");

if (!withdrawForm || !passwordInput || !withdrawModalElement || !confirmButton) {
    throw new Error("회원탈퇴 폼을 찾을 수 없습니다.");
}

function openWithdrawModal() {
    clearError(passwordInput);

    if (!validateWithdrawPassword() || !withdrawForm.reportValidity()) {
        passwordInput.focus();
        return;
    }

    bootstrap.Modal
        .getOrCreateInstance(withdrawModalElement)
        .show();
}

function validateWithdrawPassword() {
    let message = "";

    if (!isRequired(passwordInput.value)) {
        message = "비밀번호를 입력해주세요.";
    } else if (!isLengthInRange(passwordInput.value, 6, 64)) {
        message = "비밀번호는 6자 이상 64자 이하여야 합니다.";
    }

    if (message !== "") {
        setError(passwordInput, message);
        return false;
    }

    return true;
}

async function submitWithdraw() {
    confirmButton.disabled = true;

    try {
        await submitRequest(passwordInput.value);

        alert("회원탈퇴가 완료되었습니다.");
        window.location.replace("/login");
    } catch (error) {
        bootstrap.Modal
            .getOrCreateInstance(withdrawModalElement)
            .hide();

        setError(passwordInput, error.message);
        passwordInput.select();
    } finally {
        confirmButton.disabled = false;
    }
}

async function submitRequest(password) {
    const response = await fetch("/withdraw", {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ password })
    });

    if (response.status !== 204) {
        let message = "회원탈퇴에 실패했습니다.";

        try {
            const data = await response.json();
            message = data.error?.message ?? data.message ?? message;
        } catch {
            // 응답 본문이 JSON이 아닌 경우 기본 메시지 사용
        }

        throw new Error(message);
    }
}

passwordInput.addEventListener("input", () => {
    clearError(passwordInput, "비밀번호는 6자 이상 64자 이하여야 합니다.");
});

withdrawForm.addEventListener("submit", event => {
    event.preventDefault();
    openWithdrawModal();
});

confirmButton.addEventListener("click", submitWithdraw);
