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
        setError(input, "조식 소개는 255자 이내로 작성해야 합니다.");
        return false;
    }

    return true;
}
