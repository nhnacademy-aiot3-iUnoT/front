
console.log('environment.js 로드됨');

document.addEventListener('DOMContentLoaded', () => {
    const medicineRows = document.querySelectorAll('.medicine-row');

    medicineRows.forEach((row) => {
        const selectMedicine = () => {
            const url = row.dataset.url;

            if (!url) {
                return;
            }

            window.location.href = url;
        };

        row.addEventListener('click', selectMedicine);

        row.addEventListener('keydown', (event) => {
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                selectMedicine();
            }
        });
    });
});