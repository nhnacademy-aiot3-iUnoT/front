document.addEventListener("DOMContentLoaded", () => {
    const SENSOR_LABELS = {
        temperature: "온도",
        humidity: "습도",
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
        return "default";
    };

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

    groupedHistory.forEach((items, type) => {
        const option = document.createElement("option");
        option.value = type;
        option.textContent = `${sensorLabel(type)}${items[0]?.unit ? ` (${items[0].unit})` : ""}`;
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

        const unit = items[items.length - 1].unit;
        const current = items[items.length - 1].value;
        const previous = items.length > 1 ? items[items.length - 2].value : current;
        const difference = current - previous;

        latestValue.textContent = `${formatSensorValue(current)}${unit ? ` ${unit}` : ""}`;
        change.textContent = Math.abs(difference) < 0.05 ? "변화 없음" : `${difference > 0 ? "+" : ""}${formatSensorValue(difference)} ${unit}`;
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

    select.addEventListener("change", (event) => renderChart(event.target.value));

    if (groupedHistory.size) {
        select.value = groupedHistory.keys().next().value;
        renderChart(select.value);
    } else {
        renderChart("");
    }
});
