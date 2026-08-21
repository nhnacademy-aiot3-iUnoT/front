// 가상 센서 생성 / 수정 폼 검증

document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("#virtualSensorCreateForm");

    if (!form) {
        return;
    }

    const sensorTypeCheckboxes = Array.from(
        form.querySelectorAll(".sensor-type-checkbox")
    );
    const sensorTypeFeedback = form.querySelector("#sensorTypeFeedback");
    const selectedSensorCount = form.querySelector("#selectedSensorCount");
    const sensorConfigEmpty = form.querySelector("#sensorConfigEmpty");
    const sensorConfigList = form.querySelector("#sensorConfigList");

    function updateSelectionSummary() {
        const selectedCount = sensorTypeCheckboxes.filter(checkbox => checkbox.checked).length;

        if (selectedSensorCount) {
            selectedSensorCount.textContent = selectedCount + "개 선택";
            selectedSensorCount.classList.toggle("bg-primary-lt", selectedCount > 0);
            selectedSensorCount.classList.toggle("bg-secondary-lt", selectedCount === 0);
        }

        if (sensorConfigEmpty) {
            sensorConfigEmpty.classList.toggle("d-none", selectedCount > 0);
        }

        if (sensorConfigList) {
            sensorConfigList.classList.toggle("d-none", selectedCount === 0);
        }
    }

    function updateGenerationModeFields(sensorFields) {
        const modeSelect = sensorFields.querySelector(".generation-mode-select");

        if (!modeSelect) {
            return;
        }

        const sensorEnabled = !modeSelect.disabled;

        sensorFields.querySelectorAll("[data-mode-fields]").forEach(modeFields => {
            const active = sensorEnabled
                && modeFields.dataset.modeFields === modeSelect.value;

            modeFields.querySelectorAll("input, select").forEach(control => {
                control.disabled = !active;
            });
            modeFields.classList.toggle("d-none", !active);
        });
    }

    function updateSensorTypeFields(checkbox) {
        const sensorType = checkbox.dataset.sensorType;
        const fields = form.querySelector(`[data-sensor-fields="${sensorType}"]`);

        if (!fields) {
            return;
        }

        fields.querySelectorAll("input, select").forEach(control => {
            control.disabled = !checkbox.checked;
        });

        fields.classList.toggle("d-none", !checkbox.checked);
        updateGenerationModeFields(fields);
    }

    sensorTypeCheckboxes.forEach(checkbox => {
        updateSensorTypeFields(checkbox);
        checkbox.addEventListener("change", function () {
            updateSensorTypeFields(checkbox);
            updateSelectionSummary();

            if (sensorTypeCheckboxes.some(item => item.checked)) {
                sensorTypeFeedback.hidden = true;
                sensorTypeFeedback.textContent = "";
            }
        });
    });

    updateSelectionSummary();

    form.querySelectorAll(".generation-mode-select").forEach(modeSelect => {
        modeSelect.addEventListener("change", function () {
            updateGenerationModeFields(modeSelect.closest("[data-sensor-fields]"));
        });
    });

    form.addEventListener("submit", function (event) {
        clearErrors(form);

        const deviceEuiValid = validateDeviceEuiField(
            form.querySelector("#deviceEui")
        );

        const intervalValid = validateMeasurementIntervalField(
            form.querySelector("#measurementIntervalSeconds")
        );

        const selectedSensorTypesValid = sensorTypeCheckboxes.length === 0
            || sensorTypeCheckboxes.some(checkbox => checkbox.checked);

        if (!selectedSensorTypesValid) {
            sensorTypeFeedback.textContent = "센서 타입을 하나 이상 선택하세요.";
            sensorTypeFeedback.hidden = false;
        }

        const temperatureValid = validateSelectedRange(
            "#temperatureMin", "#temperatureMax", "온도"
        );

        const humidityValid = validateSelectedRange(
            "#humidityMin", "#humidityMax", "습도"
        );

        const illuminationValid = validateSelectedRange(
            "#illuminationMin", "#illuminationMax", "밝기"
        );

        const temperatureFixedValid = validateSelectedNumber(
            "#temperatureFixedValue", "온도 고정값"
        );

        const humidityFixedValid = validateSelectedNumber(
            "#humidityFixedValue", "습도 고정값"
        );

        const illuminationFixedValid = validateSelectedNumber(
            "#illuminationFixedValue", "밝기 고정값"
        );

        const doorProbabilityInput = form.querySelector("#doorProbability");
        const doorProbabilityValid = !doorProbabilityInput
            || doorProbabilityInput.disabled
            || validateDoorOpenProbabilityField(doorProbabilityInput);

        if (!(deviceEuiValid
            && intervalValid
            && selectedSensorTypesValid
            && temperatureValid
            && humidityValid
            && illuminationValid
            && temperatureFixedValid
            && humidityFixedValid
            && illuminationFixedValid
            && doorProbabilityValid)) {
            event.preventDefault();
        }
    });

    function validateSelectedRange(minSelector, maxSelector, label) {
        const minInput = form.querySelector(minSelector);
        const maxInput = form.querySelector(maxSelector);

        if (!minInput || !maxInput || minInput.disabled || maxInput.disabled) {
            return true;
        }

        return validateRangeFields(minInput, maxInput, label);
    }

    function validateSelectedNumber(selector, label) {
        const input = form.querySelector(selector);

        if (!input || input.disabled) {
            return true;
        }

        if (!isNumber(input.value)) {
            setError(input, label + "을 숫자로 입력하세요.");
            return false;
        }

        return true;
    }
});
