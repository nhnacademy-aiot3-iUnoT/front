document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("disposal-form");
    const quantityInput =
        document.getElementById("disposal-quantity");
    const reasonSelect =
        document.getElementById("disposal-reason");
    const memoGroup =
        document.getElementById("disposal-memo-group");
    const memoInput =
        document.getElementById("disposal-memo");

    const modalElement =
        document.getElementById("disposal-confirm-modal");
    const confirmButton =
        document.getElementById("confirm-disposal-button");

    const confirmQuantity =
        document.getElementById("confirm-disposal-quantity");
    const confirmReason =
        document.getElementById("confirm-disposal-reason");
    const confirmMemoRow =
        document.getElementById("confirm-disposal-memo-row");
    const confirmMemo =
        document.getElementById("confirm-disposal-memo");

    if (!form || !quantityInput || !reasonSelect ||
        !memoGroup || !memoInput || !modalElement ||
        !confirmButton) {
        return;
    }

    const updateMemoField = () => {
        const isOther = reasonSelect.value === "OTHER";

        memoGroup.hidden = !isOther;
        memoInput.disabled = !isOther;
        memoInput.required = isOther;

        if (!isOther) {
            memoInput.value = "";
        }
    };

    reasonSelect.addEventListener("change", updateMemoField);
    updateMemoField();

    form.addEventListener("submit", event => {
        event.preventDefault();

        if (!form.reportValidity()) {
            return;
        }

        const selectedReason =
            reasonSelect.options[reasonSelect.selectedIndex];

        confirmQuantity.textContent = quantityInput.value;
        confirmReason.textContent = selectedReason.text.trim();

        const isOther = reasonSelect.value === "OTHER";

        confirmMemoRow.hidden = !isOther;
        confirmMemo.textContent =
            isOther ? memoInput.value.trim() : "";

        bootstrap.Modal
            .getOrCreateInstance(modalElement)
            .show();
    });

    confirmButton.addEventListener("click", () => {
        confirmButton.disabled = true;
        form.submit();
    });
});