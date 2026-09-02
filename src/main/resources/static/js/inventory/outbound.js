document.addEventListener("DOMContentLoaded", () => {
    const form =
        document.getElementById("outbound-form");

    const quantityInput =
        document.getElementById("outbound-quantity");

    const reasonSelect =
        document.getElementById("outbound-reason");

    const memoGroup =
        document.getElementById("outbound-memo-group");

    const memoInput =
        document.getElementById("outbound-memo");

    const memoLabel =
        document.getElementById("outbound-memo-label");

    const modalElement =
        document.getElementById("outbound-confirm-modal");

    const confirmButton =
        document.getElementById("confirm-outbound-button");

    const confirmQuantity =
        document.getElementById("confirm-outbound-quantity");

    const confirmReason =
        document.getElementById("confirm-outbound-reason");

    const confirmMemoRow =
        document.getElementById("confirm-outbound-memo-row");

    const confirmMemo =
        document.getElementById("confirm-outbound-memo");

    if (!form
        || !quantityInput
        || !reasonSelect
        || !memoGroup
        || !memoInput
        || !memoLabel
        || !modalElement
        || !confirmButton
        || !confirmQuantity
        || !confirmReason
        || !confirmMemoRow
        || !confirmMemo) {
        return;
    }

    const requiresMemo = () =>
        reasonSelect.value === "STORAGE_TRANSFER"
        || reasonSelect.value === "OTHER";

    const updateMemoField = () => {
        const reason = reasonSelect.value;
        const required = requiresMemo();

        memoGroup.hidden = !required;
        memoInput.disabled = !required;
        memoInput.required = required;

        if (reason === "STORAGE_TRANSFER") {
            memoLabel.textContent = "이관 목적지";
            memoInput.placeholder =
                "이관할 저장소 또는 부서를 입력하세요.";
        } else if (reason === "OTHER") {
            memoLabel.textContent = "상세 사유";
            memoInput.placeholder =
                "구체적인 출고 사유를 입력하세요.";
        }

        if (!required) {
            memoInput.value = "";
        }
    };

    reasonSelect.addEventListener("change", () => {
        updateMemoField();
        clearFieldError(reasonSelect);
        clearFieldError(memoInput);

        validateRequiredSelectField(
            reasonSelect,
            "출고 사유"
        );
    });

    updateMemoField();

    quantityInput.addEventListener("input", () => {
        clearFieldError(quantityInput);

        validateInventoryQuantityField(
            quantityInput,
            Number(quantityInput.max)
        );
    });

    memoInput.addEventListener("input", () => {
        clearFieldError(memoInput);

        validateConditionalMemoField(
            memoInput,
            requiresMemo(),
            reasonSelect.value === "STORAGE_TRANSFER"
                ? "이관 목적지"
                : "상세 사유"
        );
    });

    form.addEventListener("submit", event => {
        event.preventDefault();
        clearErrors(form);

        const maxQuantity = Number(quantityInput.max);
        const memoRequired = requiresMemo();

        const quantityValid =
            validateInventoryQuantityField(
                quantityInput,
                maxQuantity
            );

        const reasonValid =
            validateRequiredSelectField(
                reasonSelect,
                "출고 사유"
            );

        const memoValid =
            validateConditionalMemoField(
                memoInput,
                memoRequired,
                reasonSelect.value === "STORAGE_TRANSFER"
                    ? "이관 목적지"
                    : "상세 사유"
            );

        if (!(quantityValid && reasonValid && memoValid)) {
            return;
        }

        const selectedReason =
            reasonSelect.options[
                reasonSelect.selectedIndex
                ];

        confirmQuantity.textContent =
            quantityInput.value;

        confirmReason.textContent =
            selectedReason.text.trim();

        confirmMemoRow.hidden = !memoRequired;
        confirmMemo.textContent =
            memoRequired
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