function openWithdrawModal() {
    const password = document.querySelector('input[name="password"]');

    if (!password.checkValidity()) {
        password.reportValidity();   // 브라우저의 required 메시지 표시
        password.focus();
        return;
    }

    const modal = bootstrap.Modal.getOrCreateInstance(
        document.getElementById("withdrawModal")
    );

    modal.show();
}

function submitWithdraw() {
    document.getElementById("withdraw-form").submit();
}
