
document.addEventListener('DOMContentLoaded', () => {
// 검색 종류에 따른 placeholder 변경
    const searchInput = document.querySelector('#medicine-search');
    const searchTypeInputs = document.querySelectorAll(
        'input[name="searchType"]'
    );

    searchTypeInputs.forEach((radio) => {
        radio.addEventListener('change', () => {
            if (!searchInput) {
                return;
            }

            searchInput.placeholder = radio.value === 'PRODUCT_NAME'
                ? '제품명을 입력하세요.'
                : '품목기준코드를 입력하세요.';

            searchInput.value = '';
            searchInput.focus();
        });
    });

// 검색 결과 행 전체를 클릭하면 의약품 상세로 이동
    const medicineRows = document.querySelectorAll('.medicine-row');

    medicineRows.forEach((row) => {
        const moveToDetail = () => {
            if (row.dataset.url) {
                const url = new URL(row.dataset.url, window.location.origin);

                if (storageSelect?.value) {
                    url.searchParams.set('storageId', storageSelect.value);
                }

                if (zoneSelect?.value) {
                    url.searchParams.set('zoneId', zoneSelect.value);
                }

                window.location.href = url.toString();
            }
        };

        row.addEventListener('click', moveToDetail);

        row.addEventListener('keydown', (event) => {
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                moveToDetail();
            }
        });
    });

// 저장소를 선택하면 해당 저장소의 구역 목록만 JSON으로 조회
    const storageSelect = document.querySelector('#storage-id');
    const zoneSelect = document.querySelector('#zone-id');
    const zoneMessage = document.querySelector('#zone-message');
    const searchStorageInput = document.querySelector('#search-storage-id');
    const searchZoneInput = document.querySelector('#search-zone-id');
    let selectedZoneThresholds = new Map();

    storageSelect?.addEventListener('change', async () => {
        const storageId = storageSelect.value;

        if (searchStorageInput) {
            searchStorageInput.value = storageId;
        }

        if (searchZoneInput) {
            searchZoneInput.value = '';
        }

        selectedZoneThresholds = new Map();
        updateAllEnvironmentCards();

        resetZoneSelect(
            storageId
                ? '보관 구역을 불러오는 중입니다'
                : '먼저 저장소를 선택하세요'
        );

        hideZoneMessage();

        if (!storageId) {
            return;
        }

        try {
            const response = await fetch(`/storages/${storageId}/zones`);

            if (!response.ok) {
                throw new Error(`구역 조회 실패: ${response.status}`);
            }

            const result = await response.json();
            // Front Controller가 List를 바로 반환하면 result,
            // ApiResponse를 반환하면 result.data를 사용한다.
            const zones = Array.isArray(result) ? result : result.data;

            if (!Array.isArray(zones) || zones.length === 0) {
                resetZoneSelect('사용 가능한 보관 구역이 없습니다');
                showZoneMessage('이 저장소에는 사용 가능한 보관 구역이 없습니다.');
                return;
            }

            zoneSelect.innerHTML = '';
            zoneSelect.appendChild(createOption('', '보관 구역을 선택하세요'));

            zones.forEach((zone) => {
                // ZoneInfoResponse의 실제 필드명이 id/zoneName이라면 아래 두 줄을 변경한다.
                const zoneId = zone.zoneId ?? zone.id;
                const zoneName = zone.name ?? zone.zoneName;

                zoneSelect.appendChild(createOption(zoneId, zoneName));
            });

            zoneSelect.disabled = false;

            // 검색 또는 상세조회 전 선택했던 구역을 다시 선택한다.
            const savedStorageId = storageSelect.dataset.selectedId;
            const savedZoneId = zoneSelect.dataset.selectedId;

            if (savedZoneId && (!savedStorageId || savedStorageId === storageId)) {
                zoneSelect.value = savedZoneId;

                if (searchZoneInput) {
                    searchZoneInput.value = savedZoneId;
                }

                zoneSelect.dispatchEvent(new Event('change'));
            }
        } catch (error) {
            console.error(error);
            resetZoneSelect('보관 구역을 불러오지 못했습니다');
            showZoneMessage('보관 구역을 불러오지 못했습니다.');
        }
    });

// 구역을 선택하면 해당 구역의 온도/습도/조도 임계값을 조회한다.
    zoneSelect?.addEventListener('change', async () => {
        const zoneId = zoneSelect.value;

        if (searchZoneInput) {
            searchZoneInput.value = zoneId;
        }

        selectedZoneThresholds = new Map();
        updateAllEnvironmentCards();

        if (!zoneId) {
            return;
        }

        try {
            const response = await fetch(`/zones/${zoneId}/zone-thresholds`);

            if (!response.ok) {
                throw new Error(`구역 임계값 조회 실패: ${response.status}`);
            }

            const result = await response.json();
            const thresholds = Array.isArray(result) ? result : result.data;

            if (!Array.isArray(thresholds)) {
                throw new Error('구역 임계값 응답 형식이 올바르지 않습니다.');
            }

            thresholds.forEach((threshold) => {
                const type = threshold.sensorTypeName;

                if (isMedicineEnvironmentType(type)) {
                    selectedZoneThresholds.set(type, threshold);
                }
            });

            updateAllEnvironmentCards();
        } catch (error) {
            console.error(error);
            selectedZoneThresholds = new Map();
            updateAllEnvironmentCards(true);
        }
    });

// 신규 환경 기준 입력값이 바뀌면 화면의 충족 여부만 즉시 다시 계산한다.
    document.querySelectorAll('[data-environment-input] input').forEach((input) => {
        input.addEventListener('input', () => {
            updateAllEnvironmentCards();
        });
    });

    function updateAllEnvironmentCards(loadFailed = false) {
        document.querySelectorAll('.environment-card[data-environment-type]')
            .forEach((card) => updateExistingEnvironmentCard(card, loadFailed));

        document.querySelectorAll('[data-environment-input]')
            .forEach((group) => updateInputEnvironmentGroup(group, loadFailed));

        updateEnvironmentSummary(loadFailed);
    }

    function updateExistingEnvironmentCard(card, loadFailed) {
        const type = card.dataset.environmentType;
        const medicineRange = card.querySelector('[data-medicine-min]');
        const inputGroup = document.querySelector(
            `[data-environment-input][data-environment-type="${type}"]`
        );
        const medicineMin = medicineRange
            ? toNumber(medicineRange.dataset.medicineMin)
            : toNumber(inputGroup?.querySelector('[data-environment-min]')?.value);
        const medicineMax = medicineRange
            ? toNumber(medicineRange.dataset.medicineMax)
            : toNumber(inputGroup?.querySelector('[data-environment-max]')?.value);

        const hasNoMedicineCriterion = !medicineRange && !inputGroup;

        // 기존 기준이 없는 경우 입력한 최소/최대 값을 위 환경 카드에 즉시 표시한다.
        if (inputGroup) {
            updateMedicineRangeText(card, type, medicineMin, medicineMax);
        }

        updateEnvironmentDisplay(
            card,
            type,
            medicineMin,
            medicineMax,
            loadFailed,
            hasNoMedicineCriterion
        );
    }

    function updateInputEnvironmentGroup(group, loadFailed) {
        const type = group.dataset.environmentType;
        const minInput = group.querySelector('[data-environment-min]');
        const maxInput = group.querySelector('[data-environment-max]');
        const medicineMin = toNumber(minInput?.value);
        const medicineMax = toNumber(maxInput?.value);

        updateEnvironmentDisplay(group, type, medicineMin, medicineMax, loadFailed);
    }

    function updateMedicineRangeText(card, type, medicineMin, medicineMax) {
        const displayElement = card.querySelector('[data-medicine-range]')
            ?? card.querySelector('.environment-range:last-child strong');

        if (!displayElement) {
            return;
        }

        if (medicineMin == null && medicineMax == null) {
            displayElement.textContent = `설정된 ${getEnvironmentLabel(type)}값이 없습니다.`;
            return;
        }

        const minText = medicineMin == null ? '-' : medicineMin;
        const maxText = medicineMax == null ? '-' : medicineMax;

        displayElement.textContent = `${minText} ~ ${maxText} ${getEnvironmentUnit(type)}`;
    }

    function updateEnvironmentDisplay(
        container,
        type,
        medicineMin,
        medicineMax,
        loadFailed,
        hasNoMedicineCriterion = false
    ) {
        const rangeElement = container.querySelector('[data-zone-range]');
        const statusElement = container.querySelector('[data-environment-status]');

        if (!rangeElement || !statusElement) {
            return;
        }

        clearStatusClass(statusElement);

        if (loadFailed) {
            rangeElement.textContent = '구역 기준 조회 실패';
            setStatus(statusElement, '판단 불가', 'is-pending');
            return;
        }

        if (!zoneSelect?.value) {
            rangeElement.textContent = '구역을 선택하세요';
            setStatus(statusElement, '구역 선택 전', 'is-pending');
            return;
        }

        const threshold = selectedZoneThresholds.get(type);

        if (!threshold) {
            rangeElement.textContent = '구역 기준 없음';
            setStatus(statusElement, '판단 불가', 'is-pending');
            return;
        }

        const zoneMin = toNumber(threshold.minValue);
        const zoneMax = toNumber(threshold.maxValue);
        const unit = getEnvironmentUnit(type);

        if (zoneMin == null || zoneMax == null) {
            rangeElement.textContent = '구역 기준 없음';
            setStatus(statusElement, '판단 불가', 'is-pending');
            return;
        }

        rangeElement.textContent = `${zoneMin} ~ ${zoneMax} ${unit}`;

        if (medicineMin == null && medicineMax == null) {
            setStatus(
                statusElement,
                hasNoMedicineCriterion ? '의약품 기준 없음' : '기준 입력 전',
                'is-pending'
            );
            return;
        }

        if (medicineMin == null || medicineMax == null) {
            setStatus(statusElement, '최소·최대 모두 입력', 'is-pending');
            return;
        }

        const satisfied = zoneMin >= medicineMin && zoneMax <= medicineMax;

        setStatus(
            statusElement,
            satisfied ? '기준 충족' : '기준 미충족',
            satisfied ? 'is-satisfied' : 'is-unsatisfied'
        );
    }

    function updateEnvironmentSummary(loadFailed) {
        const summary = document.querySelector('#environment-summary');
        const title = summary?.querySelector('[data-summary-title]');
        const description = summary?.querySelector('[data-summary-description]');

        if (!summary || !title || !description) {
            return;
        }

        summary.classList.remove('is-satisfied', 'is-unsatisfied', 'is-pending');

        if (loadFailed) {
            title.textContent = '구역 환경 기준을 확인하지 못했습니다.';
            description.textContent = '잠시 후 구역을 다시 선택해 주세요.';
            summary.classList.add('is-pending');
            return;
        }

        if (!zoneSelect?.value) {
            title.textContent = '보관 구역을 선택해 주세요.';
            description.textContent = '구역을 선택하면 환경 기준 충족 여부를 확인할 수 있습니다.';
            summary.classList.add('is-pending');
            return;
        }

        const statuses = Array.from(
            document.querySelectorAll('.environment-card [data-environment-status]')
        ).map((element) => element.textContent);

        if (statuses.includes('기준 미충족')) {
            title.textContent = '선택한 보관 구역은 환경 기준을 충족하지 않습니다.';
            description.textContent = '기준 미충족 항목을 확인한 뒤 다른 구역을 선택해 주세요.';
            summary.classList.add('is-unsatisfied');
            return;
        }

        if (statuses.includes('판단 불가') || statuses.includes('최소·최대 모두 입력')) {
            title.textContent = '일부 환경 기준을 비교할 수 없습니다.';
            description.textContent = '의약품 기준과 구역 임계값이 모두 등록되어 있는지 확인해 주세요.';
            summary.classList.add('is-pending');
            return;
        }

        if (statuses.includes('기준 충족')) {
            title.textContent = '선택한 보관 구역은 설정된 환경 기준을 충족합니다.';
            description.textContent = '의약품에 설정된 환경 항목을 기준으로 비교했습니다.';
            summary.classList.add('is-satisfied');
            return;
        }

        title.textContent = '비교할 수 있는 의약품 환경 기준이 없습니다.';
        description.textContent = '환경 기준을 입력하거나 구역 임계값 설정을 확인해 주세요.';
        summary.classList.add('is-pending');
    }

    function setStatus(element, text, className) {
        clearStatusClass(element);
        element.textContent = text;
        element.classList.add(className);
    }

    function clearStatusClass(element) {
        element.classList.remove('is-satisfied', 'is-unsatisfied', 'is-pending');
    }

    function isMedicineEnvironmentType(type) {
        return type === 'TEMPERATURE'
            || type === 'HUMIDITY'
            || type === 'ILLUMINATION';
    }

    function getEnvironmentUnit(type) {
        if (type === 'TEMPERATURE') {
            return '℃';
        }

        if (type === 'HUMIDITY') {
            return '%';
        }

        if (type === 'ILLUMINATION') {
            return 'lx';
        }

        return '';
    }

    function getEnvironmentLabel(type) {
        if (type === 'TEMPERATURE') {
            return '온도';
        }

        if (type === 'HUMIDITY') {
            return '습도';
        }

        if (type === 'ILLUMINATION') {
            return '조도';
        }

        return '환경 기준';
    }

    function toNumber(value) {
        if (value == null || value === '') {
            return null;
        }

        const number = Number(value);
        return Number.isFinite(number) ? number : null;
    }

    function createOption(value, text) {
        const option = document.createElement('option');
        option.value = value ?? '';
        option.textContent = text ?? '';
        return option;
    }

    function resetZoneSelect(message) {
        if (!zoneSelect) {
            return;
        }

        zoneSelect.disabled = true;
        zoneSelect.innerHTML = '';
        zoneSelect.appendChild(createOption('', message));
    }

    function showZoneMessage(message) {
        if (!zoneMessage) {
            return;
        }

        zoneMessage.textContent = message;
        zoneMessage.hidden = false;
    }

    function hideZoneMessage() {
        if (!zoneMessage) {
            return;
        }

        zoneMessage.textContent = '';
        zoneMessage.hidden = true;
    }

// 서버가 Model에 넣어준 기존 저장소 선택값을 페이지 진입 시 복원한다.
    const savedStorageId = storageSelect?.dataset.selectedId;

    if (storageSelect && savedStorageId) {
        storageSelect.value = savedStorageId;
        storageSelect.dispatchEvent(new Event("change"));
    }


    const form = document.getElementById("inbound-form");
    const modal = document.getElementById("summaryModal");

    const openButton = document.getElementById("openSummaryButton");
    const closeButton = document.getElementById("closeSummaryButton");
    const confirmButton = document.getElementById("confirmInboundButton");

    if (form && modal && openButton && closeButton && confirmButton) {

        openButton.addEventListener("click", () => {

            if (!form.checkValidity()) {
                form.reportValidity();
                return;
            }

            const productName =
                document.getElementById("selectedProductName")?.textContent.trim() ?? "";

            const storageName =
                storageSelect?.selectedOptions[0]?.textContent.trim() ?? "";

            const zoneName =
                zoneSelect?.selectedOptions[0]?.textContent.trim() ?? "";

            const lotNumber =
                form.querySelector("[name='lotNumber']")?.value ?? "";

            const expirationDate =
                form.querySelector("[name='expirationDate']")?.value ?? "";

            const quantity =
                form.querySelector("[name='quantity']")?.value ?? "";

            const memo =
                form.querySelector("[name='memo']")?.value ?? "";


            document.getElementById("summaryProductName").textContent =
                productName;

            document.getElementById("summaryStorageName").textContent =
                storageName;

            document.getElementById("summaryZoneName").textContent =
                zoneName;

            document.getElementById("summaryLotNumber").textContent =
                lotNumber;

            document.getElementById("summaryExpirationDate").textContent =
                expirationDate;

            document.getElementById("summaryQuantity").textContent =
                quantity;




            modal.classList.add("show");
        });

        closeButton.addEventListener("click", () => {
            modal.classList.remove("show");
        });

        confirmButton.addEventListener("click", () => {
            form.requestSubmit();
        });
    }


});

