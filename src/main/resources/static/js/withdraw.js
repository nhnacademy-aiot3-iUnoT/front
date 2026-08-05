const withdrawForm = document.getElementById("withdraw-form");
const passwordInput = withdrawForm.querySelector('input[name="password"]');
const withdrawModalElement = document.getElementById("withdrawModal");
const submitButton = withdrawModalElement.querySelector(".btn-danger");

function openWithdrawModal() {
    if (!withdrawForm.reportValidity()) {
        passwordInput.focus();
        return;
    }

    bootstrap.Modal
        .getOrCreateInstance(withdrawModalElement)
        .show();
}

async function submitWithdraw() {
    submitButton.disabled = true;

    try {
        await submitRequest(passwordInput.value);

        alert("회원탈퇴가 완료되었습니다.");
        window.location.replace("/login");
    } catch (error) {
        bootstrap.Modal
            .getOrCreateInstance(withdrawModalElement)
            .hide();

        passwordInput.setCustomValidity(error.message);
        passwordInput.reportValidity();
        passwordInput.setCustomValidity("");
        passwordInput.select();
    } finally {
        submitButton.disabled = false;
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

    if (!response.ok) {
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

withdrawForm.addEventListener("submit", event => {
    event.preventDefault();
    openWithdrawModal();
});