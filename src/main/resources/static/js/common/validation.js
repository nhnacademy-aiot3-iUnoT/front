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

function isDeviceEui(value) {
    return /^[A-Za-z0-9_-]+$/.test(value);
}

function isNumber(value) {
    return isRequired(value) && !Number.isNaN(Number(value));
}

function isPositiveInteger(value) {
    return Number.isInteger(Number(value)) && Number(value) >= 1;
}

function isInRange(value, min, max) {
    return isNumber(value) && Number(value) >= min && Number(value) <= max;
}
