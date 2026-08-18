document.addEventListener("DOMContentLoaded", function () {
    const weekPickerInput = document.getElementById("week-picker");
    if (!weekPickerInput) {
        return;
    }

    const currentStart = weekPickerInput.dataset.currentStart || "";

    // 선택된 날짜가 속한 주의 월요일 계산
    function getMonday(d) {
        const date = new Date(d);
        const day = date.getDay();
        const diff = date.getDate() - day + (day === 0 ? -6 : 1);
        return new Date(date.setDate(diff));
    }

    // 주간 기간 문자열 포맷팅 (YYYY.MM.DD (월) ~ MM.DD (일))
    function formatWeekRange(monday) {
        const sunday = new Date(monday);
        sunday.setDate(monday.getDate() + 6);

        const pad = (n) => String(n).padStart(2, "0");

        const mYear = monday.getFullYear();
        const mMonth = pad(monday.getMonth() + 1);
        const mDate = pad(monday.getDate());

        const sYear = sunday.getFullYear();
        const sMonth = pad(sunday.getMonth() + 1);
        const sDate = pad(sunday.getDate());

        if (mYear === sYear) {
            return `${mYear}.${mMonth}.${mDate} (월) ~ ${sMonth}.${sDate} (일)`;
        }
        return `${mYear}.${mMonth}.${mDate} (월) ~ ${sYear}.${sMonth}.${sDate} (일)`;
    }

    flatpickr(weekPickerInput, {
        locale: {
            ...(flatpickr.l10ns.ko || {}),
            firstDayOfWeek: 1
        },
        plugins: [new weekSelect({})],
        defaultDate: currentStart,
        maxDate: "today",
        dateFormat: "Y-m-d",
        onReady: function (selectedDates, dateStr, instance) {
            if (selectedDates && selectedDates.length > 0) {
                const monday = getMonday(selectedDates[0]);
                instance.input.value = formatWeekRange(monday);
            }
        },
        onChange: function (selectedDates, dateStr, instance) {
            if (selectedDates && selectedDates.length > 0) {
                const monday = getMonday(selectedDates[0]);
                instance.input.value = formatWeekRange(monday);

                const year = monday.getFullYear();
                const month = String(monday.getMonth() + 1).padStart(2, "0");
                const d = String(monday.getDate()).padStart(2, "0");
                const formattedMonday = `${year}-${month}-${d}`;

                if (formattedMonday !== currentStart) {
                    window.location.href = `/reports/weekly?periodStart=${formattedMonday}`;
                }
            }
        }
    });
});
