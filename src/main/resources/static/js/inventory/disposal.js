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
    const confirmMessage =
        document.getElementById("disposal-confirm-message");
    const expirationWarning =
        document.getElementById("expiration-warning");
    const confirmExpirationDate =
        document.getElementById("confirm-expiration-date");

    // 확인 모달의 요소가 누락됐을 때 JavaScript 오류가 발생하지 않도록 막는다
    if (!form || !quantityInput || !reasonSelect
        || !memoGroup || !memoInput || !modalElement
        || !confirmButton || !confirmQuantity
        || !confirmReason || !confirmMemoRow
        || !confirmMemo || !expirationWarning
        || !confirmExpirationDate || !confirmMessage) {
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

    const getTodayText = () => {
        const today = new Date();

        return [
            today.getFullYear(),
            String(today.getMonth() + 1).padStart(2, "0"),
            String(today.getDate()).padStart(2, "0")
        ].join("-");
    };

    reasonSelect.addEventListener("change", () => {
        updateMemoField();
        clearError(reasonSelect);
        clearError(memoInput);

        validateRequiredSelectField(
            reasonSelect,
            "폐기 사유"
        );
    });

    updateMemoField();

    quantityInput.addEventListener("input", () => {
        clearError(quantityInput);

        validateInventoryQuantityField(
            quantityInput,
            Number(quantityInput.max)
        );
    });

    memoInput.addEventListener("input", () => {
        clearError(memoInput);

        validateConditionalMemoField(
            memoInput,
            reasonSelect.value === "OTHER",
            "상세 사유"
        );
    });

    form.addEventListener("submit", event => {
        event.preventDefault();
        clearErrors(form);

        const maxQuantity = Number(quantityInput.max);
        const isOther = reasonSelect.value === "OTHER";

        const quantityValid =
            validateInventoryQuantityField(
                quantityInput,
                maxQuantity
            );

        const reasonValid =
            validateRequiredSelectField(
                reasonSelect,
                "폐기 사유"
            );

        const memoValid =
            validateConditionalMemoField(
                memoInput,
                isOther,
                "상세 사유"
            );

        if (!(quantityValid && reasonValid && memoValid)) {
            return;
        }

        const expirationDate = form.dataset.expirationDate;
        const isUnexpiredExpiredDisposal =
            reasonSelect.value === "EXPIRED"
            && expirationDate >= getTodayText();
        confirmMessage.hidden = isUnexpiredExpiredDisposal;

        expirationWarning.hidden = !isUnexpiredExpiredDisposal;
        confirmExpirationDate.textContent =
            isUnexpiredExpiredDisposal ? expirationDate : "";

        const selectedReason =
            reasonSelect.options[
                reasonSelect.selectedIndex
                ];

        confirmQuantity.textContent =
            quantityInput.value;

        confirmReason.textContent =
            selectedReason.text.trim();

        confirmMemoRow.hidden = !isOther;
        confirmMemo.textContent =
            isOther
                ? memoInput.value.trim()
                : "";

        bootstrap.Modal
            .getOrCreateInstance(modalElement)
            .show();
    });

    confirmButton.addEventListener("click", () => {
        confirmButton.disabled = true;
        form.submit();
    });
});