// 이메일 필드 검증
function validateEmailField(input) {
    const value = input.value.trim();

    if (!isRequired(value)) {
        setError(input, "이메일은 필수 입력입니다.");
        return false;
    }

    if (!isEmail(value)) {
        setError(input, "올바른 이메일 형식이 아닙니다.");
        return false;
    }
    return true;
}

// 사업자 번호 필드 검증
function validateBusinessNumberField(input) {
    const value = input.value.trim();

    if (!isRequired(value)) {
        setError(input, "사업자 번호는 필수 입력입니다.");
        return false;
    }

    if (!isBusinessNumber(value)) {
        setError(input, "사업자 번호는 숫자 10자리입니다.");
        return false;
    }
    return true;
}

// 조직명 필드 검증
function validateOrganizationNameField(input) {
    const value = input.value;

    if(!isRequired(value)) {
        setError(input, "조직명은 필수 입력입니다.");
        return false;
    }

    if(!isMaxLength(value, 50)) {
        setError(input, "조직명은 50자 이내로 작성해야합니다.");
        return false;
    }
    return true;
}

// 우편번호 필드 검증
function validateZipCodeField(input) {
    const value = input.value.trim();

    if(!isRequired(value)) {
        setError(input, "우편번호는 필수 입력입니다.");
        return false;
    }

    if(!isZipCode(value)) {
        setError(input, "우편번호는 숫자 5자리입니다.");
        return false;
    }

    return true;
}

// 도로명 주소 필드 검증
function validateRoadAddress(input) {
    const value = input.value;

    if(!isRequired(value)) {
        setError(input, "도로명 주소는 필수 입력입니다.");
        return false;
    }

    if(!isMaxLength(value, 255)) {
        setError(input, "도로명 주소는 255자 이내로 작성해야 합니다.");
        return false;
    }
    return true;
}

// 상세 주소 필드 길이 검증
function validateAddressDetail(input) {
    const value = input.value;

    if(!isMaxLength(value, 255)) {
        setError(input, "상세 주소는 255자 이내로 작성해야 합니다.");
        return false;
    }

    return true;
}

// 조직 소개 필드 길이 검증
function validateDescription(input) {
    const value = input.value;

    if(!isMaxLength(value, 255)) {
        setError(input, "소개/설명은 255자 이내로 작성해야 합니다.");
        return false;
    }

    return true;
}

// 디바이스 EUI 필드 검증
function validateDeviceEuiField(input) {
    const value = input.value.trim();

    if (!isRequired(value)) {
        setError(input, "디바이스 EUI는 필수 입력입니다.");
        return false;
    }

    if (!isMaxLength(value, 64)) {
        setError(input, "디바이스 EUI는 64자 이내로 작성해야 합니다.");
        return false;
    }

    if (!isDeviceEui(value)) {
        setError(input, "디바이스 EUI는 영문, 숫자, -, _ 만 사용할 수 있습니다.");
        return false;
    }

    return true;
}

// 측정 주기 필드 검증
function validateMeasurementIntervalField(input) {
    const value = input.value.trim();

    if (!isRequired(value)) {
        setError(input, "측정 주기는 필수 입력입니다.");
        return false;
    }

    if (!isPositiveInteger(value)) {
        setError(input, "측정 주기는 1초 이상의 정수여야 합니다.");
        return false;
    }

    return true;
}

// 최솟값/최댓값 한 쌍을 검증한다. 범위 오류는 최댓값 쪽에 표시한다.
function validateRangeFields(minInput, maxInput, label) {
    let valid = true;

    if (!isNumber(minInput.value)) {
        setError(minInput, label + " 최솟값을 숫자로 입력하세요.");
        valid = false;
    }

    if (!isNumber(maxInput.value)) {
        setError(maxInput, label + " 최댓값을 숫자로 입력하세요.");
        valid = false;
    }

    if (!valid) {
        return false;
    }

    if (Number(minInput.value) > Number(maxInput.value)) {
        setError(maxInput, label + " 최솟값은 최댓값보다 클 수 없습니다.");
        return false;
    }

    return true;
}

// 문 열림 확률 필드 검증
function validateDoorOpenProbabilityField(input) {
    const value = input.value.trim();

    if (!isRequired(value)) {
        setError(input, "문 열림 확률은 필수 입력입니다.");
        return false;
    }

    if (!isInRange(value, 0, 1)) {
        setError(input, "문 열림 확률은 0 이상 1 이하여야 합니다.");
        return false;
    }

    return true;
}

// 부서명 (필수, 30자)
function validateDepartmentNameField(input) {
    const value = input.value.trim();

    if(!isRequired(value)) {
        setError(input, "부서명은 필수 입력입니다.");
        return false;
    }

    if(!isMaxLength(value, 30)) {
        setError(input, "부서명은 30자 이내로 작성해야합니다.");
        return false;
    }

    return true;
}

// 재고 수량 필드 검증
function validateInventoryQuantityField(input, maxQuantity) {
    if (maxQuantity < 1) {
        setError(input, "처리 가능한 재고가 없습니다.");
        return false;
    }

    const value = input.value.trim();

    if (!isRequired(value)) {
        setError(input, "수량을 입력해주세요.");
        return false;
    }

    if (!isPositiveInteger(value)) {
        setError(input, "수량은 1개 이상의 정수로 입력해주세요.");
        return false;
    }

    if (!isInRange(value, 1, maxQuantity)) {
        setError(input, `수량은 1개 이상 ${maxQuantity}개 이하로 입력해주세요.`);
        return false;
    }

    return true;
}

function validateRequiredSelectField(input, label) {
    if (!isRequired(input.value)) {
        setError(input, `${label} 선택이 필요합니다.`);
        return false;
    }

    return true;
}

function validateConditionalMemoField(input, required, label, maxLength = 100) {
    const value = input.value.trim();

    if (required && !isRequired(value)) {
        setError(input, `${label} 입력이 필요합니다.`);
        return false;
    }

    if (!isMaxLength(value, maxLength)) {
        setError(input, `${label}: ${maxLength}자 이내로 입력해주세요.`);
        return false;
    }

    return true;
}
