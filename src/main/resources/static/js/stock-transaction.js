document.addEventListener("DOMContentLoaded", () => {
    const modalElement = document.getElementById("stock-transaction-modal");

    if (!modalElement || typeof bootstrap === "undefined") {
        return;
    }

    const modal = new bootstrap.Modal(modalElement);

    // 값이 없으면 빈 칸 대신 '-'를 넣어 항목이 사라지지 않게 한다.
    const setText = (id, value) => {
        const element = document.getElementById(id);

        if (element) {
            element.textContent = (value === null || value === undefined || value === "") ? "-" : value;
        }
    };

    const formatDateTime = (value) => {
        if (!value) {
            return "-";
        }

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return value;
        }

        return new Intl.DateTimeFormat("ko-KR", {
            year: "numeric", month: "2-digit", day: "2-digit",
            hour: "2-digit", minute: "2-digit"
        }).format(date);
    };

    document.querySelectorAll(".stock-transaction-row").forEach((row) => {
        row.addEventListener("click", () => {
            const data = row.dataset;

            setText("detail-processed-at", formatDateTime(data.processedAt));
            setText("detail-medicine", data.medicine);
            setText("detail-pack-unit", data.packUnit);
            setText("detail-type", data.type);
            setText("detail-quantity", data.quantity);
            setText("detail-reason", data.reason);
            setText("detail-memo", data.memo);
            setText("detail-processed-by", data.processedBy);

            modal.show();
        });
    });
});

function clearZoneAndSubmit(select) {
    const form = select.form;
    const zoneInput = form.querySelector('input[name="zone-id"]');

    if (zoneInput) {
        zoneInput.value = "";
    }

    form.submit();
}
