// 필수값, 최대길이, 정규식 검증

function isRequired(value) {
    return value !== null && value.trim().length > 0;
}

function isMaxLength(value, max) {
    return value.length <= max;
}

function isEmail(value) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

function isBusinessNumber(value) {
    return /^\d{10}$/.test(value);
}

function isZipCode(value) {
    return /^\d{5}$/.test(value);
}
