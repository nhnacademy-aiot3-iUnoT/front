document.addEventListener("DOMContentLoaded", () => {
    // 상세 모달과 무관하므로 부트스트랩이 없어도 동작해야 한다.
    setupDateRange();

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

function setupDateRange() {
    const startInput = document.getElementById("start-date");
    const endInput = document.getElementById("end-date");

    if (!startInput || !endInput) {
        return;
    }

    // toISOString 은 UTC 로 바꾸므로 새벽에 하루 밀린다. 로컬 날짜를 직접 조립한다.
    const toValue = (date) => {
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");

        return `${date.getFullYear()}-${month}-${day}`;
    };

    // 시작일이 종료일보다 뒤면 결과가 0건으로 나와 데이터가 없는 것처럼 보인다.
    const syncBounds = () => {
        endInput.min = startInput.value;
        startInput.max = endInput.value;
    };

    startInput.addEventListener("change", syncBounds);
    endInput.addEventListener("change", syncBounds);
    syncBounds();

    document.querySelectorAll("[data-range]").forEach((button) => {
        button.addEventListener("click", () => {
            const days = Number(button.dataset.range);
            const end = new Date();
            const start = new Date();

            // 오늘을 포함해 days 일이 되도록 하루를 뺀다. (7일 = 오늘 포함 7일)
            start.setDate(end.getDate() - Math.max(days - 1, 0));

            startInput.value = toValue(start);
            endInput.value = toValue(end);
            syncBounds();

            startInput.form.submit();
        });
    });
}
