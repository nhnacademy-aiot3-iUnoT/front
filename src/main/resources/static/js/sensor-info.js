document.addEventListener("DOMContentLoaded", () => {
    const SENSOR_LABELS = {
        temperature: "온도",
        humidity: "습도",
        door: "문 상태",
        illumination: "조도"
    };

    const normalizeType = (type) => (type || "unknown").trim().toLowerCase();

    const sensorLabel = (type) => {
        const normalized = normalizeType(type);
        return SENSOR_LABELS[normalized] || type.replaceAll("_", " ");
    };

    const sensorCategory = (type) => {
        const normalized = normalizeType(type);
        if (normalized.includes("temp")) return "temperature";
        if (normalized.includes("humid")) return "humidity";
        if (["light", "illumination", "illuminance"].some((key) => normalized.includes(key))) return "light";
        if (normalized === "door") return "door";
        return "default";
    };

    const isDoorSensor = (type) => normalizeType(type) === "door";

    const normalizeDoorValue = (value) => Number(value) >= 0.5 ? 1 : 0;

    const formatDateTime = (value) => {
        if (!value) return "측정 시간 없음";
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return value;
        return new Intl.DateTimeFormat("ko-KR", {
            month: "short",
            day: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        }).format(date);
    };

    const formatSensorValue = (value, maximumFractionDigits = 1) => {
        const number = Number(value);
        if (!Number.isFinite(number)) return "-";

        return number.toLocaleString("ko-KR", {
            minimumFractionDigits: 0,
            maximumFractionDigits
        });
    };

    const displayUnit = (unit) => (unit || "").trim();

    document.querySelectorAll(".sensor-card").forEach((card) => {
        const type = card.dataset.sensorType || "센서";
        card.dataset.sensorCategory = sensorCategory(type);
        const name = card.querySelector(".sensor-name");
        if (name) name.textContent = sensorLabel(type);
        const time = card.querySelector("time");
        if (time) time.textContent = formatDateTime(time.getAttribute("datetime"));
    });

    // 새로고침 버튼 클릭시 현재 페이지 새로고침
    document.getElementById("refresh-button")?.addEventListener("click", () => window.location.reload());

    const history = Array.from(document.querySelectorAll("#sensor-history-data [data-sensor-type]"))
        .map((element) => ({
            sensorType: element.dataset.sensorType,
            unit: element.dataset.unit || "",
            time: element.dataset.time,
            value: Number(element.dataset.value)
        }))
        .filter((item) => item.sensorType && item.time && Number.isFinite(item.value));

    const groupedHistory = history.reduce((groups, item) => {
        if (!groups.has(item.sensorType)) groups.set(item.sensorType, []);
        groups.get(item.sensorType).push(item);
        return groups;
    }, new Map());

    groupedHistory.forEach((items) => items.sort((a, b) => new Date(a.time) - new Date(b.time)));

    const select = document.getElementById("sensor-select");
    const empty = document.getElementById("chart-empty");
    const canvas = document.getElementById("sensor-chart");
    const metric = document.getElementById("chart-metric");
    const latestValue = document.getElementById("chart-latest-value");
    const change = document.getElementById("chart-change");
    let chart;

    // 그래프 선택 목록에는 문 센서를 포함하지 않는다 (문 센서는 열림 기록 섹션에서 별도 표시)
    groupedHistory.forEach((items, type) => {
        if (isDoorSensor(type)) return;

        const option = document.createElement("option");
        const unit = displayUnit(items[0]?.unit);
        option.value = type;
        option.textContent = `${sensorLabel(type)}${unit ? ` (${unit})` : ""}`;
        select.append(option);
    });

    const renderChart = (type) => {
        chart?.destroy();
        chart = undefined;

        const items = groupedHistory.get(type) || [];
        if (!items.length || typeof Chart === "undefined") {
            empty.style.display = "flex";
            canvas.style.display = "none";
            metric.hidden = true;
            return;
        }

        empty.style.display = "none";
        canvas.style.display = "block";
        metric.hidden = false;

        const unit = displayUnit(items[items.length - 1].unit);
        const current = items[items.length - 1].value;
        const previous = items.length > 1 ? items[items.length - 2].value : current;
        const difference = current - previous;

        latestValue.textContent = `${formatSensorValue(current)}${unit ? ` ${unit}` : ""}`;

        change.textContent = Math.abs(difference) < 0.05
            ? "변화 없음"
            : `${difference > 0 ? "+" : ""}${formatSensorValue(difference)} ${unit}`;
        change.className = `chart-metric-change ${difference > 0 ? "text-red" : difference < 0 ? "text-blue" : "text-secondary"}`;

        const styles = getComputedStyle(document.documentElement);

        const textColor =
            styles.getPropertyValue("--tblr-secondary-color").trim() || "#667382";

        const gridColor =
            styles.getPropertyValue("--tblr-border-color").trim() || "#e6e7e9";

        const category = sensorCategory(type);

        const sensorColors = {
            temperature: styles.getPropertyValue("--sensor-temperature").trim(),
            humidity: styles.getPropertyValue("--sensor-humidity").trim(),
            light: styles.getPropertyValue("--sensor-light").trim(),
            default: styles.getPropertyValue("--sensor-default").trim()
        };

        const primaryColor =
            sensorColors[category] || sensorColors.default || "#0ca678";

        chart = new Chart(canvas, {
            type: "line",
            data: {
                labels: items.map((item) => new Intl.DateTimeFormat("ko-KR", {
                    hour: "2-digit",
                    minute: "2-digit"
                }).format(new Date(item.time))),
                datasets: [{
                    label: sensorLabel(type),
                    data: items.map((item) => item.value),
                    borderColor: primaryColor,
                    backgroundColor: `${primaryColor}18`,
                    borderWidth: 2,
                    pointRadius: items.length > 30 ? 0 : 2.5,
                    pointHoverRadius: 5,
                    pointBackgroundColor: primaryColor,
                    fill: true,
                    tension: .35
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: { intersect: false, mode: "index" },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        displayColors: false,
                        callbacks: {
                            label: (context) => `${formatSensorValue(context.parsed.y)} ${unit}`
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: { color: textColor, maxTicksLimit: 8, maxRotation: 0 }
                    },
                    y: {
                        beginAtZero: false,
                        border: { display: false },
                        grid: { color: gridColor },
                        ticks: {
                            color: textColor,
                            precision: 1,
                            callback: (value) => `${formatSensorValue(value)}${unit}`
                        }
                    }
                }
            }
        });
    };

    // 문 센서 이력에서 "닫힘 -> 열림 -> 닫힘" 구간만 추출하여 열림 이벤트 목록을 만든다.
    const buildDoorOpenEvents = (items) => {
        const events = [];
        let prevState = null;
        let openStart = null;

        items.forEach((item) => {
            const state = normalizeDoorValue(item.value);

            if (state === 1 && prevState !== 1) {
                openStart = item.time; // 열림 시작
            }
            if (state === 0 && prevState === 1 && openStart) {
                events.push({ start: openStart, end: item.time });
                openStart = null;
            }

            prevState = state;
        });

        // 마지막까지 열려있는 상태로 끝난 경우
        if (openStart) {
            events.push({ start: openStart, end: null });
        }

        return events.reverse(); // 최신순
    };

    // 문 센서 데이터가 있는 경우에만 문 열림 기록 섹션을 표시한다.
    const DOOR_HISTORY_PAGE_SIZE = 10;
    let doorHistoryEvents = [];
    let doorHistoryPage = 1;

    const doorHistoryListEl = document.getElementById("door-history-list");
    const doorHistoryPaginationEl = document.getElementById("door-history-pagination");
    const doorHistoryPageInfoEl = document.getElementById("door-history-page-info");
    const doorHistoryPrevBtn = document.getElementById("door-history-prev");
    const doorHistoryNextBtn = document.getElementById("door-history-next");

    const renderDoorHistoryPage = () => {
        const totalPages = Math.max(1, Math.ceil(doorHistoryEvents.length / DOOR_HISTORY_PAGE_SIZE));
        doorHistoryPage = Math.min(Math.max(1, doorHistoryPage), totalPages);

        const start = (doorHistoryPage - 1) * DOOR_HISTORY_PAGE_SIZE;
        const pageEvents = doorHistoryEvents.slice(start, start + DOOR_HISTORY_PAGE_SIZE);

        if (!doorHistoryEvents.length) {
            doorHistoryListEl.innerHTML = `<p class="text-secondary mb-0">최근 24시간 동안 문 열림 기록이 없습니다.</p>`;
            doorHistoryPaginationEl.hidden = true;
            return;
        }

        doorHistoryListEl.innerHTML = pageEvents.map((event) => `
            <div class="door-history-item">
                <i class="ti ti-door"></i>
                <span>${formatDateTime(event.start)}</span>
                ${event.end
            ? `<span class="text-secondary"> ~ ${formatDateTime(event.end)}</span>`
            : `<span class="badge bg-red-lt ms-1">열림 중</span>`}
            </div>
        `).join("");

        doorHistoryPaginationEl.hidden = totalPages <= 1;
        doorHistoryPageInfoEl.textContent = `${doorHistoryPage} / ${totalPages}`;
        doorHistoryPrevBtn.disabled = doorHistoryPage <= 1;
        doorHistoryNextBtn.disabled = doorHistoryPage >= totalPages;
    };

    doorHistoryPrevBtn?.addEventListener("click", () => {
        doorHistoryPage -= 1;
        renderDoorHistoryPage();
    });

    doorHistoryNextBtn?.addEventListener("click", () => {
        doorHistoryPage += 1;
        renderDoorHistoryPage();
    });

    const renderDoorHistory = () => {
        const doorType = [...groupedHistory.keys()].find((type) => isDoorSensor(type));
        const section = document.getElementById("door-history-section");

        if (!doorType) {
            section.hidden = true;
            return;
        }

        doorHistoryEvents = buildDoorOpenEvents(groupedHistory.get(doorType));
        doorHistoryPage = 1;

        const countBadge = document.getElementById("door-history-count");
        section.hidden = false;
        countBadge.textContent = `${doorHistoryEvents.length}건`;

        renderDoorHistoryPage();
    };

    select.addEventListener("change", (event) => renderChart(event.target.value));

    const firstNonDoorType = [...groupedHistory.keys()].find((type) => !isDoorSensor(type));

    if (firstNonDoorType) {
        select.value = firstNonDoorType;
        renderChart(firstNonDoorType);
    } else {
        renderChart("");
    }

    renderDoorHistory();
});