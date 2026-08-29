// --- 모달 제어 ---
function showZoneModal(id) { bootstrap.Modal.getOrCreateInstance(document.getElementById(id)).show(); }
function openEditZoneModal() { const modal = document.getElementById('edit-zone-modal'); clearErrors(modal); showZoneModal('edit-zone-modal'); document.getElementById('edit-zone-name').focus(); }
function openSensorModal() { const modal = document.getElementById('sensor-modal'); clearErrors(modal); showZoneModal('sensor-modal'); document.getElementById('sensor-eui').focus(); }
function openThresholdModal() { showZoneModal('threshold-modal'); loadSensorTypes(); }

// --- 이벤트 연결용 중간 함수 ---
function onToggleZoneStatusClick(btn) { toggleZoneStatus(btn.dataset.storageId, btn.dataset.zoneId, btn.dataset.status); }
function onDeleteZoneClick(btn) { deleteZone(btn.dataset.storageId, btn.dataset.zoneId); }
function onUpdateZoneInfoClick(btn) { updateZoneInfo(btn.dataset.storageId, btn.dataset.zoneId); }
function onCreateZoneSensorClick(btn) { createZoneSensor(btn.dataset.zoneId); }

// --- API 로직 ---
function updateZoneInfo(storageId, zoneId) {
    const form = document.getElementById('edit-zone-modal');
    const name = document.getElementById('edit-zone-name').value.trim();
    const description = document.getElementById('edit-zone-description').value.trim();
    clearErrors(form);
    if (!isRequired(name)) {
        setError(document.getElementById('edit-zone-name'), '구역 이름을 입력해주세요.');
        return;
    }

    if (!isMaxLength(name, 30)) {
        setError(document.getElementById('edit-zone-name'), '구역 이름은 최대 30자까지 입력 가능합니다.');
        return;
    }

    if (!isMaxLength(description, 255)) {
        setError(document.getElementById('edit-zone-description'), '설명은 최대 255자까지 입력 가능합니다.');
        return;
    }

    apiFetch(`/api/core/storages/${storageId}/zones/${zoneId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, description: description || null })
    }).then(res => res.ok ? location.reload() : alert('실패'));
}

function toggleZoneStatus(storageId, zoneId, status) {
    apiFetch(`/api/core/storages/${storageId}/zones/${zoneId}/status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' })
    }).then(res => res.ok ? location.reload() : alert('실패'));
}

function deleteZone(storageId, zoneId) {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    apiFetch(`/api/core/storages/${storageId}/zones/${zoneId}`, {
        method: 'DELETE'
    })
        .then(res => res.ok ? location.href = `/storages/${storageId}` : alert('실패'));
}

function createZoneSensor(zoneId) {
    const form = document.getElementById('sensor-modal');
    const euiInput = document.getElementById('sensor-eui');
    const nameInput = document.getElementById('sensor-name');
    const descriptionInput = document.getElementById('sensor-description');
    const eui = euiInput.value.trim();
    const name = nameInput.value.trim();
    const description = descriptionInput.value.trim();

    clearErrors(form);
    if (!isRequired(eui) || !isDeviceEui(eui)) {
        setError(euiInput, 'Device EUI를 올바르게 입력해주세요.');
        return;
    }

    if (!isMaxLength(eui, 50)) {
        setError(euiInput, 'Device EUI는 최대 50자까지 입력 가능합니다.');
        return;
    }

    if (!isRequired(name)) {
        setError(nameInput, '센서 이름을 입력해주세요.');
        return;
    }

    if (!isMaxLength(name, 50)) {
        setError(nameInput, '센서 이름은 최대 50자까지 입력 가능합니다.');
        return;
    }
    if (!isMaxLength(description, 255)) {
        setError(descriptionInput, '설명은 최대 255자까지 입력 가능합니다.');
        return;
    }

    apiFetch(`/api/core/zones/${zoneId}/zone-sensors`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            deviceEui: eui, name, description: description || null
        })
    }).then(res => res.ok ? location.reload() : alert('실패'));
}

function loadSensorTypes() {
    apiFetch(`/api/core/sensor-types`)
        .then(res => res.json())
        .then(result => {
            const selectBox = document.getElementById('threshold-sensor-type');
            selectBox.innerHTML = '';
            if (result.success && Array.isArray(result.data)) {
                result.data.forEach(t => {
                    const option = document.createElement('option');
                    option.value = t.sensorTypeId;
                    option.textContent = t.name;
                    selectBox.appendChild(option);
                });
                onThresholdSensorTypeChange();
            }
        })
        .catch(err => console.error('센서 타입 로드 실패:', err));
}

// SensorType이 Door일 경우 최소/최대값은 설정하지 않음
function isDoorSensorType() {
    const selectBox = document.getElementById('threshold-sensor-type');
    const selectedType = selectBox.options[selectBox.selectedIndex];
    return selectedType?.textContent.trim().toLowerCase() === 'door';
}

function onThresholdSensorTypeChange() {
    const disabled = isDoorSensorType();
    const minInput = document.getElementById('threshold-min-value');
    const maxInput = document.getElementById('threshold-max-value');
    minInput.disabled = disabled;
    maxInput.disabled = disabled;

    if (disabled) {
        minInput.value = '';
        maxInput.value = '';
    }
}

document.getElementById('btn-create-threshold').addEventListener('click', function() {
    const form = document.getElementById('threshold-modal');
    const minInput = document.getElementById('threshold-min-value');
    const maxInput = document.getElementById('threshold-max-value');
    const delayInput = document.getElementById('threshold-delay');
    const min = minInput.value.trim();
    const max = maxInput.value.trim();
    const delay = delayInput.value.trim();

    clearErrors(form);

    if (!isDoorSensorType() && !isNumber(min)) {
        setError(minInput, '최소값을 입력해주세요.');
        return;
    }

    if (!isDoorSensorType() && !isNumber(max)) {
        setError(maxInput, '최대값을 입력해주세요.');
        return;
    }

    if (!isDoorSensorType() && Number(min) > Number(max)) {
        setError(maxInput, '최대값은 최소값보다 크거나 같아야 합니다.');
        return;
    }

    if (!isPositiveInteger(delay)) {
        setError(delayInput, '임계 시간은 1 이상의 정수로 입력해주세요.');
        return;
    }

    const zoneId = document.getElementById('modal-zone-id').value;
    const requestData = {
        sensorTypeId: Number(document.getElementById('threshold-sensor-type').value),
        minValue: isDoorSensorType() ? null : Number(min),
        maxValue: isDoorSensorType() ? null : Number(max),
        alertDuration: Number(delay)
    };

    apiFetch(`/api/core/zones/${zoneId}/zone-thresholds`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData)
    })
        .then(res => {
            if (res.ok) {
                alert('임계값이 설정되었습니다.');
                location.reload();
            } else {
                alert('임계값 설정 실패');
            }
        })
        .catch(err => console.error('임계값 저장 에러:', err));
});
