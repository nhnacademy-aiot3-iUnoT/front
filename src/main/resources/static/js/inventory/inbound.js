
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
    const medicineDetail = document.querySelector('#medicine-detail');
    const storageTemperatureRange = parseTemperatureRange(medicineDetail?.dataset.storageMethod ?? '');
    const storageSelect = document.querySelector('#storage-id');
    const zoneSelect = document.querySelector('#zone-id');
    const selectedZoneInput = document.querySelector('#selected-zone-id');
    const zoneMessage = document.querySelector('#zone-message');
    const searchStorageInput = document.querySelector('#search-storage-id');
    const searchZoneInput = document.querySelector('#search-zone-id');

    const savedStorageId = storageSelect?.dataset.selectedId;
    const savedZoneId = zoneSelect?.dataset.selectedId || selectedZoneInput?.value;



    let selectedZoneThresholds = new Map();



    storageSelect?.addEventListener('change', async () => {
        const storageId = storageSelect.value;


        // 사용자가 기존 저장소가 아닌 다른 저장소를 선택한 경우
        if (
            selectedZoneInput
            && savedStorageId
            && storageId !== savedStorageId
        ) {
            selectedZoneInput.value = '';
            zoneSelect.dataset.selectedId = '';
        }


        // 기존 구역 조회 로직
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
            console.log('구역 목록 요청:', `/storages/${storageId}/zones`);
            const response = await fetch(`/storages/${storageId}/zones`);

            if (!response.ok) {
                throw new Error(`구역 조회 실패: ${response.status}`);
            }

            const result = await response.json();
            // Front Controller가 List를 바로 반환하면 result,
            // ApiResponse를 반환하면 result.data를 사용한다.
            const zones = Array.isArray(result) ? result : result.data;

            // 입고 화면에는 활성 구역만 표시
            const activeZones = Array.isArray(zones)
                ? zones.filter(zone => zone.status === 'ACTIVE')
                : [];

            if (activeZones.length === 0) {
                resetZoneSelect('사용 가능한 보관 구역이 없습니다');
                showZoneMessage('이 저장소에는 사용 가능한 보관 구역이 없습니다.');
                return;
            }

            zoneSelect.innerHTML = '';
            zoneSelect.appendChild(createOption('', '보관 구역을 선택하세요'));

            activeZones.forEach((zone) => {
                const zoneId = zone.zoneId ?? zone.id;
                const zoneName = zone.name ?? zone.zoneName;

                zoneSelect.appendChild(createOption(zoneId, zoneName));
            });

            zoneSelect.disabled = false;

            // 검색 또는 상세조회 전 선택했던 구역을 다시 선택한다.


            if (savedZoneId && (!savedStorageId || savedStorageId === storageId)) {
                zoneSelect.value = savedZoneId;

                if (searchZoneInput) {
                    searchZoneInput.value = savedZoneId;
                }

                if (selectedZoneInput){
                    selectedZoneInput.value = savedZoneId;

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


        console.log('구역 change 실행:', zoneId);


        if (selectedZoneInput) {
            selectedZoneInput.value = zoneId;
        }

        if (searchZoneInput) {
            searchZoneInput.value = zoneId;
        }

        selectedZoneThresholds = new Map();
        updateAllEnvironmentCards();

        if (!zoneId) {
            console.log('zoneId가 비어 있어 조회 중단');
            return;
        }



        try {
            console.log('임계값 요청:', `/zones/${zoneId}/zone-thresholds`);

            const response = await fetch(`/zones/${zoneId}/zone-thresholds`);

            console.log('임계값 응답:', response.status);

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

    function updateAllEnvironmentCards(card, loadFailed) {
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
        // 별도로 등록된 온도 기준이 없을 때만 보관방법의 숫자 범위를 사용한다.
        const storageMethodRange =
            type === 'TEMPERATURE' && !medicineRange && !inputGroup
                ? storageTemperatureRange
                : null;
        const medicineMin = medicineRange
            ? toNumber(medicineRange.dataset.medicineMin)
            : inputGroup
                ? toNumber(
                    inputGroup.querySelector('[data-environment-min]')?.value
                )
                : storageMethodRange?.min ?? null;

        const medicineMax = medicineRange
            ? toNumber(medicineRange.dataset.medicineMax)
            : inputGroup
                ? toNumber(
                    inputGroup.querySelector('[data-environment-max]')?.value
                )
                : storageMethodRange?.max ?? null;

        const hasNoMedicineCriterion =
            !medicineRange && !inputGroup && !storageMethodRange;

        // 기존 기준이 없는 경우 입력한 최소/최대 값을 위 환경 카드에 즉시 표시한다.
        if (inputGroup || storageMethodRange) {
            updateMedicineRangeText(card, type, medicineMin, medicineMax, Boolean(storageMethodRange));
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

    function updateMedicineRangeText(card, type, medicineMin, medicineMax, fromStorageMethod = false) {
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
        const sourceText = fromStorageMethod ? ' · 보관방법 기준' : '';

        displayElement.textContent = `${minText} ~ ${maxText} ${getEnvironmentUnit(type)}${sourceText}`;
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

        if (medicineMin == null && medicineMax == null) {
            setStatus(statusElement, '비교 기준 미등록', 'is-pending');
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
            description.textContent = '보관방법 또는 별도로 등록된 수치 기준으로 비교했습니다.';
            summary.classList.add('is-satisfied');
            return;
        }

        title.textContent = '비교를 위한 환경 기준이 등록되지 않았습니다.';
        description.textContent = '위 보관방법은 참고용 정보이며, 등록한 수치를 기준으로 사용합니다.';
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

    function createRangeValidator({
                                      minId,
                                      maxId,
                                      pairMessage,
                                      rangeMessage
                                  }) {
        const minInput = document.getElementById(minId);
        const maxInput = document.getElementById(maxId);


        // 해당 환경 기준 입력창이 렌더링되지 않은 경우
        if (!minInput || !maxInput) {
            console.log('환경 입력창 없음:', {
                minId,
                maxId,
                minInput,
                maxInput
            });

            return () => true;
        }



        function validate() {
            const minValue = minInput.value.trim();
            const maxValue = maxInput.value.trim();

            // 이전 검증 메시지 제거
            minInput.setCustomValidity('');
            maxInput.setCustomValidity('');

            // 둘 다 비어 있으면 정상
            if (minValue === '' && maxValue === '') {
                return true;
            }

            // 최대값만 입력한 경우 → 최소값에 오류 표시
            if (minValue === '') {
                minInput.setCustomValidity(pairMessage);
                return false;
            }

            // 최소값만 입력한 경우 → 최대값에 오류 표시
            if (maxValue === '') {
                maxInput.setCustomValidity(pairMessage);
                return false;
            }

            // 최소값이 최대값보다 큰 경우
            if (Number(minValue) > Number(maxValue)) {
                maxInput.setCustomValidity(rangeMessage);
                return false;
            }

            return true;
        }

        minInput.addEventListener('input', validate);
        maxInput.addEventListener('input', validate);

        validate();

        return validate;
    }


    const environmentValidators = [
        createRangeValidator({
            minId: 'min-temperature',
            maxId: 'max-temperature',
            pairMessage: '온도 최소값과 최대값 모두 입력해주세요.',
            rangeMessage: '온도 최소값은 최대값보다 클 수 없습니다.'
        }),

        createRangeValidator({
            minId: 'min-humidity',
            maxId: 'max-humidity',
            pairMessage: '습도 최소값과 최대값 모두 입력해주세요.',
            rangeMessage: '습도 최소값은 최대값보다 클 수 없습니다.'
        }),

        createRangeValidator({
            minId: 'min-illuminance',
            maxId: 'max-illuminance',
            pairMessage: '조도 최소값과 최대값 모두 입력해주세요.',
            rangeMessage: '조도 최소값은 최대값보다 클 수 없습니다.'
        })
    ];


    const expirationInput =
        document.getElementById('expiration-date');

    function validateExpirationDate() {
        if (!expirationInput) {
            return true;
        }

        expirationInput.setCustomValidity('');

        if (expirationInput.value === '') {
            expirationInput.setCustomValidity(
                '유통기한을 입력해주세요.'
            );

            return false;
        }

        if (
            expirationInput.min
            && expirationInput.value < expirationInput.min
        ) {
            expirationInput.setCustomValidity(
                '실제 유통기한은 현재 날짜 이후여야 합니다.'
            );

            return false;
        }

        return true;
    }

    expirationInput?.addEventListener(
        'input',
        validateExpirationDate
    );

    expirationInput?.addEventListener(
        'change',
        validateExpirationDate
    );

    function parseTemperatureRange(storageMethod) {
        if (!storageMethod) {
            return null;
        }

        const match = storageMethod.match(
            /(-?\d+(?:\.\d+)?)\s*(?:~|～|∼|–|—|-)\s*(-?\d+(?:\.\d+)?)\s*(?:℃|°C)/i
        );

        if (!match) {
            return null;
        }

        const min = Number(match[1]);
        const max = Number(match[2]);

        if (!Number.isFinite(min) || !Number.isFinite(max) || min > max) {
            return null;
        }

        return { min, max };
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

    function updateEnvironmentModalSummary() {
        const section = document.getElementById(
            'summaryEnvironmentSection'
        );

        if (!section) {
            return;
        }

        const environmentValues = [
            {
                rowId: 'summaryTemperatureRow',
                summaryId: 'summaryTemperature',
                minId: 'min-temperature',
                maxId: 'max-temperature',
                unit: '℃'
            },
            {
                rowId: 'summaryHumidityRow',
                summaryId: 'summaryHumidity',
                minId: 'min-humidity',
                maxId: 'max-humidity',
                unit: '%'
            },
            {
                rowId: 'summaryIlluminanceRow',
                summaryId: 'summaryIlluminance',
                minId: 'min-illuminance',
                maxId: 'max-illuminance',
                unit: 'lx'
            }
        ];

        let hasEnvironmentValue = false;

        environmentValues.forEach((environment) => {
            const row = document.getElementById(environment.rowId);
            const summary = document.getElementById(
                environment.summaryId
            );
            const minInput = document.getElementById(
                environment.minId
            );
            const maxInput = document.getElementById(
                environment.maxId
            );

            if (!row || !summary) {
                return;
            }

            const min = minInput?.value.trim() ?? '';
            const max = maxInput?.value.trim() ?? '';

            // 해당 환경 기준 입력란이 없거나 두 값이 모두 비어 있으면 숨김
            if (!min && !max) {
                row.hidden = true;
                summary.textContent = '';
                return;
            }

            summary.textContent =
                `${min || '-'} ~ ${max || '-'} ${environment.unit}`;

            row.hidden = false;
            hasEnvironmentValue = true;
        });

        section.hidden = !hasEnvironmentValue;
    }



// 서버가 Model에 넣어준 기존 저장소 선택값을 페이지 진입 시 복원한다.

    if (storageSelect && savedStorageId) {
        storageSelect.value = savedStorageId;
        storageSelect.dispatchEvent(new Event("change"));
    }


    const form = document.getElementById("inbound-form");
    const modal = document.getElementById("summaryModal");

    const openButton = document.getElementById("openSummaryButton");
    const closeButton = document.getElementById("closeSummaryButton");
    const confirmButton = document.getElementById("confirmInboundButton");
    const overwriteExpirationInput = document.getElementById("overwrite-expiration-date");
    const lotExpirationWarning = document.getElementById("lot-expiration-warning");
    const existingExpirationDate = document.getElementById("existing-expiration-date");
    const enteredExpirationDate = document.getElementById("entered-expiration-date");




    if (form && modal && openButton && closeButton && confirmButton&& overwriteExpirationInput && lotExpirationWarning && existingExpirationDate && enteredExpirationDate) {

        openButton.addEventListener("click", async () => {

            environmentValidators.forEach(validate => validate());
            validateExpirationDate();

            if (!form.reportValidity()) {
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
                form.querySelector("[name='expirationDate']").value ?? "";

            const quantity =
                form.querySelector("[name='quantity']")?.value ?? "";

            const transactionTypeSelect = form.querySelector("[name='transactionType']");

            const transactionType = transactionTypeSelect?.options[transactionTypeSelect.selectedIndex]?.text ?? "";


            const memo =
                form.querySelector("[name='memo']")?.value ?? "";

            overwriteExpirationInput.value = "false";
            lotExpirationWarning.hidden = true;
            confirmButton.textContent = "입고 등록";

            const medicinePackageUnitId =
                form.querySelector(
                    "[name='medicinePackageUnitId']"
                )?.value ?? "";

            const zoneId =
                form.querySelector("[name='zoneId']")?.value ?? "";

            const params = new URLSearchParams({
                "medicine-package-unit-id":
                medicinePackageUnitId,
                "zone-id": zoneId,
                "lot-number": lotNumber
            });

            try {
                const response = await apiFetch(
                    `/api/core/inventories/inbound/lot-expiration?${params}`
                );

                if (!response.ok) {
                    const permissionNotice =
                        document.getElementById("narcotic-permission-notice");

                    if (response.status === 403 && permissionNotice) {
                        permissionNotice.classList.add("is-denied");
                        permissionNotice.scrollIntoView({
                            behavior: "smooth",
                            block: "center"
                        });
                        permissionNotice.focus({ preventScroll: true });
                        return;
                    }

                    throw new Error(
                        "기존 제조번호 조회에 실패했습니다."
                    );
                }

                const body = await response.json();
                const existing = body.data;

                if (
                    existing?.exists
                    && existing.expirationDate !== expirationDate
                ) {
                    overwriteExpirationInput.value = "true";
                    lotExpirationWarning.hidden = false;

                    existingExpirationDate.textContent =
                        existing.expirationDate;
                    enteredExpirationDate.textContent =
                        expirationDate;

                    confirmButton.textContent =
                        "정정 후 입고";
                }
            } catch (error) {
                console.error(
                    "기존 제조번호 조회 실패:",
                    error
                );

                window.alert(
                    "기존 제조번호 정보를 확인하지 못했습니다. 다시 시도해주세요."
                );
                return;
            }

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

            document.getElementById("summaryTransactionType").textContent =
                transactionType;

            document.getElementById("summaryMemo").textContent =
                memo;


            updateEnvironmentModalSummary();

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

