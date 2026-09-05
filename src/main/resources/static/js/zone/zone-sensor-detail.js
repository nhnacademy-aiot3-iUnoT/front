// 모달 열기
function openUpdateModal() {
    const modal = document.getElementById('update-modal');
    clearErrors(modal);
    bootstrap.Modal.getOrCreateInstance(modal).show();
    document.getElementById('edit-name').focus();
}

// 모달 닫기
function closeUpdateModal() {
    const modal = document.getElementById('update-modal');
    clearErrors(modal);
    bootstrap.Modal.getOrCreateInstance(modal).hide();
}

// 센서 정보 수정 API 호출
function updateZoneSensor() {
    const zoneId = document.getElementById('edit-zone-id').value;
    const sensorId = document.getElementById('edit-sensor-id').value;
    const nameInput = document.getElementById('edit-name');
    const descInput = document.getElementById('edit-description');

    const name = nameInput.value.trim();
    const description = descInput.value.trim();

    clearErrors(document.getElementById('update-modal'));
    if (!isRequired(name)) {
        setError(nameInput, '센서 이름을 입력해주세요.'); nameInput.focus();
        return;
    }

    if (!isMaxLength(name, 50)) {
        setError(nameInput, '센서 이름은 최대 50자까지 입력 가능합니다.');
        nameInput.focus();
        return;
    }

    if (!isMaxLength(description, 255)) {
        setError(descInput, '설명은 최대 255자까지 입력 가능합니다.');
        descInput.focus(); return;
    }

    apiFetch(`/api/core/zones/${zoneId}/zone-sensors/${sensorId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            name: name,
            description: description === '' ? null : description
        })
    })
        .then(response => {
            if (response.ok) {
                closeUpdateModal();
                location.reload();
            } else {
                return response.json().then(err => {
                    alert(err.message || '센서 수정에 실패했습니다.');
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 통신 중 오류가 발생했습니다.');
        });
}

// 센서 삭제 API 호출
function deleteZoneSensor(storageId, zoneId, sensorId) {
    if (!confirm('정말 이 센서를 삭제하시겠습니까?')) {
        return;
    }

    apiFetch(`/api/core/zones/${zoneId}/zone-sensors/${sensorId}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (response.ok || response.status === 204) {
                alert('센서가 삭제되었습니다.');
                location.href = `/storages/${storageId}/zones/${zoneId}`;
            } else {
                return response.json().then(err => {
                    alert(err.message || '센서 삭제에 실패했습니다.');
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 통신 중 오류가 발생했습니다.');
        });
}
