// 가상 센서 생성 / 수정 폼 검증

document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("#virtualSensorCreateForm");

    if (!form) {
        return;
    }

    form.addEventListener("submit", function (event) {
        clearErrors(form);

        const deviceEuiValid = validateDeviceEuiField(
            form.querySelector("#deviceEui")
        );

        const intervalValid = validateMeasurementIntervalField(
            form.querySelector("#measurementIntervalSeconds")
        );

        const temperatureValid = validateRangeFields(
            form.querySelector("#temperatureMin"),
            form.querySelector("#temperatureMax"),
            "온도"
        );

        const humidityValid = validateRangeFields(
            form.querySelector("#humidityMin"),
            form.querySelector("#humidityMax"),
            "습도"
        );

        const illuminationValid = validateRangeFields(
            form.querySelector("#illuminationMin"),
            form.querySelector("#illuminationMax"),
            "밝기"
        );

        const doorOpenProbabilityValid = validateDoorOpenProbabilityField(
            form.querySelector("#doorOpenProbability")
        );

        if (!(deviceEuiValid
            && intervalValid
            && temperatureValid
            && humidityValid
            && illuminationValid
            && doorOpenProbabilityValid)) {
            event.preventDefault();
        }
    });
});
