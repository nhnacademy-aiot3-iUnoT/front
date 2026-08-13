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

    function updateSensorTypeFields(checkbox) {
        const sensorType = checkbox.dataset.sensorType;
        const fields = form.querySelector(`[data-sensor-fields="${sensorType}"]`);

        if (!fields) {
            return;
        }

        fields.querySelectorAll("input").forEach(input => {
            input.disabled = !checkbox.checked;
        });

        fields.classList.toggle("opacity-50", !checkbox.checked);
    }

    sensorTypeCheckboxes.forEach(checkbox => {
        updateSensorTypeFields(checkbox);
        checkbox.addEventListener("change", function () {
            updateSensorTypeFields(checkbox);

            if (sensorTypeCheckboxes.some(item => item.checked)) {
                sensorTypeFeedback.hidden = true;
                sensorTypeFeedback.textContent = "";
            }
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

        const doorOpenProbabilityInput = form.querySelector("#doorOpenProbability");
        const doorOpenProbabilityValid = !doorOpenProbabilityInput
            || doorOpenProbabilityInput.disabled
            || validateDoorOpenProbabilityField(doorOpenProbabilityInput);

        if (!(deviceEuiValid
            && intervalValid
            && selectedSensorTypesValid
            && temperatureValid
            && humidityValid
            && illuminationValid
            && doorOpenProbabilityValid)) {
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
});
