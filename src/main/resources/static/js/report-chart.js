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

    let doors;

    try {
        doors = JSON.parse(root.dataset.doors || "[]");
    } catch (error) {
        console.warn("문 개폐 차트 데이터를 해석하지 못했습니다.", error);
        doors = [];
    }

    if (!Array.isArray(doors)) {
        doors = [];
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

    // 그날의 최저~최고를 음영 띠로 그린다.
    // 평균만 그리면 평균이 기준 안에 있는 한 최고값이 상한을 넘겨도 화면에 드러나지 않는다.
    const bandDatasets = (points, color) => ([
        {
            label: "최저~최고 범위",
            data: points.map((point) => toNumber(point.maxValue)),
            borderWidth: 0,
            pointRadius: 0,
            pointHoverRadius: 0,
            backgroundColor: `${color}20`,
            fill: "+1",
            tension: .35,
            hideInTooltip: true
        },
        {
            label: "최저",
            data: points.map((point) => toNumber(point.minValue)),
            borderWidth: 0,
            pointRadius: 0,
            pointHoverRadius: 0,
            fill: false,
            tension: .35,
            hideInLegend: true,
            hideInTooltip: true
        }
    ]);

    // 센서 차트와 문 개폐 차트를 세로로 비교하려면 x축이 같은 위치에서 시작해야 한다.
    // y축 눈금 글자 폭이 서로 달라 그냥 두면 플롯 영역이 어긋나므로 폭을 고정한다.
    const Y_AXIS_WIDTH = 56;
    const MAX_ALIGN_PASSES = 6;
    const ALIGN_TOLERANCE = 0.5;
    const fixYAxisWidth = (scale) => {
        scale.width = Y_AXIS_WIDTH;
    };

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

        const datasets = [
            ...bandDatasets(points, color),
            {
                label: `${sensorLabel(environment.sensorType)} 일평균`,
                data: points.map((point) => toNumber(point.avgValue)),
                borderColor: color,
                backgroundColor: `${color}18`,
                borderWidth: 2,
                pointRadius: 3,
                pointHoverRadius: 5,
                pointBackgroundColor: color,
                fill: false,
                tension: .35
            }
        ];

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
                        labels: {
                            color: textColor,
                            boxWidth: 12,
                            usePointStyle: true,
                            filter: (item, chartData) => !chartData.datasets[item.datasetIndex].hideInLegend
                        }
                    },
                    tooltip: {
                        displayColors: false,
                        filter: (item) => !item.dataset.hideInTooltip,
                        callbacks: {
                            // 일평균 위에 그날의 최저/최고도 함께 보여준다.
                            afterBody: (items) => {
                                const point = items.length ? points[items[0].dataIndex] : null;

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
                        offset: true,
                        grid: { display: false },
                        ticks: { color: textColor, maxRotation: 0 }
                    },
                    y: {
                        beginAtZero: false,
                        border: { display: false },
                        grid: { color: gridColor },
                        afterFit: fixYAxisWidth,
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

    const findDoor = (zoneId) =>
        doors.find((item) => String(item.zoneId) === String(zoneId));

    root.querySelectorAll(".door-chart-canvas").forEach((canvas) => {
        const door = findDoor(canvas.dataset.zoneId);

        if (!door || !Array.isArray(door.dailyPoints) || door.dailyPoints.length === 0) {
            return;
        }

        const points = door.dailyPoints;
        const color = sensorColor("door");

        const chart = new Chart(canvas, {
            type: "bar",
            data: {
                labels: points.map((point) => formatDate(point.date)),
                datasets: [
                    {
                        label: "누적 개방 시간",
                        data: points.map((point) => point.openMinutes),
                        yAxisID: "minutes",
                        backgroundColor: `${color}55`,
                        hoverBackgroundColor: `${color}66`,
                        borderWidth: 0,
                        borderRadius: 3,
                        maxBarThickness: 28,
                        order: 2
                    },
                    {
                        // 횟수는 적은데 시간이 길면 "문을 열어둔 날"이다. 그 어긋남이 보이도록 함께 그린다.
                        label: "개폐 횟수",
                        type: "line",
                        data: points.map((point) => point.openCount),
                        yAxisID: "count",
                        borderColor: color,
                        backgroundColor: color,
                        borderWidth: 2,
                        pointRadius: 3,
                        pointHoverRadius: 5,
                        fill: false,
                        tension: .35,
                        order: 1
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: { intersect: false, mode: "index" },
                plugins: {
                    legend: {
                        labels: { color: textColor, boxWidth: 12, usePointStyle: true }
                    },
                    tooltip: {
                        displayColors: false,
                        callbacks: {
                            label: (context) => (context.dataset.yAxisID === "minutes"
                                ? `누적 개방 시간: ${context.parsed.y}분`
                                : `개폐 횟수: ${context.parsed.y}회`)
                        }
                    }
                },
                scales: {
                    x: {
                        offset: true,
                        grid: { display: false },
                        ticks: { color: textColor, maxRotation: 0 }
                    },
                    minutes: {
                        position: "left",
                        beginAtZero: true,
                        border: { display: false },
                        grid: { color: gridColor },
                        afterFit: fixYAxisWidth,
                        ticks: {
                            color: textColor,
                            precision: 0,
                            callback: (value) => `${value}분`
                        }
                    },
                    count: {
                        position: "right",
                        beginAtZero: true,
                        border: { display: false },
                        grid: { display: false },
                        ticks: {
                            color: textColor,
                            precision: 0,
                            callback: (value) => `${value}회`
                        }
                    }
                }
            }
        });

        charts.push(chart);
    });

    // 같은 구역의 차트들을 세로로 비교하려면 플롯 영역이 정확히 겹쳐야 한다.
    // y축 폭은 위에서 고정했지만 오른쪽 끝은 차트 종류(선/막대)에 따라 몇 px 어긋난다.
    // 원인 값에 의존하지 않도록, 그려진 뒤 실측해서 가장 좁은 쪽에 맞춘다.
    const alignChartAreas = () => {
        const groups = new Map();

        charts.forEach((chart) => {
            const pane = chart.canvas.closest(".tab-pane") || root;

            if (!groups.has(pane)) {
                groups.set(pane, []);
            }

            groups.get(pane).push(chart);
        });

        groups.forEach((group) => {
            if (group.length < 2) {
                return;
            }

            // 중첩 속성을 고쳐 쓰면 Chart.js가 레이아웃을 다시 계산하지 않는다.
            // layout 객체를 통째로 갈아끼우고 전체 update 를 돌려야 반영된다.
            group.forEach((chart) => {
                chart.options.layout = { padding: { right: 0 } };
                chart.update();
            });

            // 숨겨진 탭은 폭이 0이라 기준으로 삼으면 안 된다.
            const visible = group.filter((chart) => chart.chartArea.width > 0);

            if (visible.length < 2) {
                return;
            }

            // padding.right 를 넣으면 Chart.js 의 autoPadding 이 그만큼 줄어들어 1:1 로 반영되지 않는다.
            // 한 번에 맞출 수 없으므로 차이가 사라질 때까지 조금씩 좁힌다. 보통 2~3회면 수렴한다.
            for (let pass = 0; pass < MAX_ALIGN_PASSES; pass += 1) {
                const rights = visible.map((chart) => chart.chartArea.right);
                const minRight = Math.min(...rights);

                if (Math.max(...rights) - minRight <= ALIGN_TOLERANCE) {
                    break;
                }

                visible.forEach((chart) => {
                    const current = chart.options.layout.padding.right || 0;

                    chart.options.layout = {
                        padding: { right: current + (chart.chartArea.right - minRight) }
                    };
                    chart.update();
                });
            }
        });
    };

    alignChartAreas();

    // 숨겨진 탭에서 만들어진 차트는 폭이 0으로 잡힌다.
    // 탭이 실제로 보이는 시점에 다시 크기를 계산하고 정렬도 다시 맞춘다.
    document.querySelectorAll('[data-bs-toggle="tab"]').forEach((tab) => {
        tab.addEventListener("shown.bs.tab", () => {
            charts.forEach((chart) => chart.resize());
            alignChartAreas();
        });
    });

    window.addEventListener("resize", () => {
        window.clearTimeout(alignChartAreas._timer);
        alignChartAreas._timer = window.setTimeout(alignChartAreas, 200);
    });
});
