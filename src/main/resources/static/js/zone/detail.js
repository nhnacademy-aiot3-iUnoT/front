// --- 모달 제어 ---
function openEditZoneModal() { document.getElementById('edit-zone-modal').style.display = 'flex'; }
function closeEditZoneModal() { document.getElementById('edit-zone-modal').style.display = 'none'; }
function openSensorModal() { document.getElementById('sensor-modal').style.display = 'flex'; }
function closeSensorModal() { document.getElementById('sensor-modal').style.display = 'none'; }
function openThresholdModal() { document.getElementById('threshold-modal').style.display = 'flex'; loadSensorTypes(); }
function closeThresholdModal() { document.getElementById('threshold-modal').style.display = 'none'; }

// --- 이벤트 연결용 중간 함수 ---
function onToggleZoneStatusClick(btn) { toggleZoneStatus(btn.dataset.storageId, btn.dataset.zoneId, btn.dataset.status); }
function onDeleteZoneClick(btn) { deleteZone(btn.dataset.storageId, btn.dataset.zoneId); }
function onUpdateZoneInfoClick(btn) { updateZoneInfo(btn.dataset.storageId, btn.dataset.zoneId); }
function onCreateZoneSensorClick(btn) { createZoneSensor(btn.dataset.zoneId); }

// --- API 로직 ---
function updateZoneInfo(storageId, zoneId) {
    const name = document.getElementById('edit-zone-name').value.trim();
    const description = document.getElementById('edit-zone-description').value.trim();
    fetch(`/api/core/storages/${storageId}/zones/${zoneId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ name, description: description || null })
    }).then(res => res.ok ? location.reload() : alert('실패'));
}

function toggleZoneStatus(storageId, zoneId, status) {
    fetch(`/api/core/storages/${storageId}/zones/${zoneId}/status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ status: status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' })
    }).then(res => res.ok ? location.reload() : alert('실패'));
}

function deleteZone(storageId, zoneId) {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    fetch(`/api/core/storages/${storageId}/zones/${zoneId}`, {
        method: 'DELETE',
        credentials: 'include'
    })
        .then(res => res.ok ? location.href = `/storages/${storageId}` : alert('실패'));
}

function createZoneSensor(zoneId) {
    fetch(`/api/core/zones/${zoneId}/zone-sensors`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
            deviceEui: document.getElementById('sensor-eui').value,
            name: document.getElementById('sensor-name').value,
            description: document.getElementById('sensor-description').value
        })
    }).then(res => res.ok ? location.reload() : alert('실패'));
}

function loadSensorTypes() {
    fetch(`/api/core/sensor-types`, {
        credentials: 'include'
    })
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
            }
        })
        .catch(err => console.error('센서 타입 로드 실패:', err));
}

document.getElementById('btn-create-threshold').addEventListener('click', function() {
    const zoneId = document.getElementById('modal-zone-id').value;
    const requestData = {
        sensorTypeId: Number(document.getElementById('threshold-sensor-type').value),
        minValue: Number(document.getElementById('threshold-min-value').value),
        maxValue: Number(document.getElementById('threshold-max-value').value),
        alertDuration: Number(document.getElementById('threshold-delay').value)
    };

    fetch(`/api/core/zones/${zoneId}/zone-thresholds`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
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