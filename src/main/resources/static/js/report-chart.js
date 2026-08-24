document.addEventListener("DOMContentLoaded", () => {
    const root = document.getElementById("environment-root");

    if (!root || typeof Chart === "undefined") {
        return;
    }

    let environments;

    try {
        environments = JSON.parse(root.dataset.environments || "[]");
    } catch (error) {
        console.warn("환경 차트 데이터를 해석하지 못했습니다.", error);
        return;
    }

    if (!Array.isArray(environments) || environments.length === 0) {
        return;
    }

    const SENSOR_CATEGORIES = {
        temperature: "temperature",
        humidity: "humidity",
        illumination: "light",
        door: "door"
    };

    const SENSOR_LABELS = {
        temperature: "온도",
        humidity: "습도",
        illumination: "조도",
        door: "문 상태"
    };

    const styles = getComputedStyle(document.documentElement);

    const cssColor = (name, fallback) =>
        styles.getPropertyValue(name).trim() || fallback;

    const textColor = cssColor("--tblr-secondary-color", "#667382");
    const gridColor = cssColor("--tblr-border-color", "#e6e7e9");
    const dangerColor = cssColor("--tblr-danger", "#d63939");

    const sensorColor = (sensorType) => {
        const key = String(sensorType || "").toLowerCase();
        const category = SENSOR_CATEGORIES[key] || "default";

        return cssColor(`--sensor-${category}`, "#0ca678");
    };

    const sensorLabel = (sensorType) => {
        const key = String(sensorType || "").toLowerCase();

        return SENSOR_LABELS[key] || String(sensorType || "").replaceAll("_", " ");
    };

    const formatDate = (value) => {
        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return value;
        }

        return new Intl.DateTimeFormat("ko-KR", { month: "numeric", day: "numeric" }).format(date);
    };

    const toNumber = (value) => (value === null || value === undefined ? null : Number(value));

    const thresholdDataset = (label, value, days) => ({
        label,
        data: new Array(days).fill(value),
        borderColor: dangerColor,
        borderWidth: 1.5,
        borderDash: [6, 4],
        pointRadius: 0,
        pointHoverRadius: 0,
        fill: false
    });

    const findEnvironment = (zoneId, sensorType) =>
        environments.find((item) =>
            String(item.zoneId) === String(zoneId) && String(item.sensorType) === String(sensorType));

    const charts = [];

    root.querySelectorAll(".env-chart-canvas").forEach((canvas) => {
        const environment = findEnvironment(canvas.dataset.zoneId, canvas.dataset.sensorType);

        if (!environment || !Array.isArray(environment.dailyPoints) || environment.dailyPoints.length === 0) {
            return;
        }

        const points = environment.dailyPoints;
        const labels = points.map((point) => formatDate(point.date));
        const color = sensorColor(environment.sensorType);
        const unit = environment.unit || "";

        const datasets = [{
            label: `${sensorLabel(environment.sensorType)} 일평균`,
            data: points.map((point) => toNumber(point.avgValue)),
            borderColor: color,
            backgroundColor: `${color}18`,
            borderWidth: 2,
            pointRadius: 3,
            pointHoverRadius: 5,
            pointBackgroundColor: color,
            fill: true,
            tension: .35
        }];

        const thresholdMin = toNumber(environment.thresholdMin);
        const thresholdMax = toNumber(environment.thresholdMax);

        if (thresholdMin !== null) {
            datasets.push(thresholdDataset("기준 하한", thresholdMin, points.length));
        }

        if (thresholdMax !== null) {
            datasets.push(thresholdDataset("기준 상한", thresholdMax, points.length));
        }

        const chart = new Chart(canvas, {
            type: "line",
            data: { labels, datasets },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: { intersect: false, mode: "index" },
                plugins: {
                    legend: {
                        display: datasets.length > 1,
                        labels: { color: textColor, boxWidth: 12, usePointStyle: true }
                    },
                    tooltip: {
                        displayColors: false,
                        callbacks: {
                            // 일평균 위에 그날의 최저/최고도 함께 보여준다.
                            afterBody: (items) => {
                                const point = points[items[0].dataIndex];

                                if (!point) {
                                    return "";
                                }

                                return `최저 ${point.minValue}${unit} / 최고 ${point.maxValue}${unit}`;
                            },
                            label: (context) => `${context.dataset.label}: ${context.parsed.y}${unit}`
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: { color: textColor, maxRotation: 0 }
                    },
                    y: {
                        beginAtZero: false,
                        border: { display: false },
                        grid: { color: gridColor },
                        ticks: {
                            color: textColor,
                            callback: (value) => `${value}${unit}`
                        }
                    }
                }
            }
        });

        charts.push(chart);
    });

    // 숨겨진 탭에서 만들어진 차트는 폭이 0으로 잡힌다.
    // 탭이 실제로 보이는 시점에 다시 크기를 계산해 준다.
    document.querySelectorAll('[data-bs-toggle="tab"]').forEach((tab) => {
        tab.addEventListener("shown.bs.tab", () => {
            charts.forEach((chart) => chart.resize());
        });
    });
});
