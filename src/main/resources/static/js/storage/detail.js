// --- 저장소 수정 모달 제어 ---
function openEditModal() {
    const modal = document.getElementById('edit-modal');
    resetEditForm();
    clearErrors(modal);
    bootstrap.Modal.getOrCreateInstance(modal).show();
    document.getElementById('edit-name').focus();
}
function closeEditModal() {
    const modal = document.getElementById('edit-modal');
    resetEditForm();
    clearErrors(modal);
    bootstrap.Modal.getOrCreateInstance(modal).hide();
}

function resetEditForm() {
    const nameInput = document.getElementById('edit-name');
    const descriptionInput = document.getElementById('edit-description');

    nameInput.value = nameInput.dataset.initialValue || '';
    descriptionInput.value = descriptionInput.dataset.initialValue || '';
}

// --- 구역 추가 모달 제어 ---
function openZoneModal() {
    bootstrap.Modal.getOrCreateInstance(document.getElementById('zone-modal')).show();
    document.getElementById('zone-name').focus();
}
function closeZoneModal() {
    const modal = document.getElementById('zone-modal');
    clearErrors(modal);
    bootstrap.Modal.getOrCreateInstance(modal).hide();
    document.getElementById('zone-name').value = '';
    document.getElementById('zone-description').value = '';
}

// --- API 호출 함수들 ---

// 1. 저장소 정보 수정
function updateStorage(storageId) {
    const form = document.getElementById('edit-modal');
    const nameInput = document.getElementById('edit-name');
    const descriptionInput = document.getElementById('edit-description');
    const name = nameInput.value.trim();
    const description = descriptionInput.value.trim();

    clearErrors(form);
    if (!isRequired(name)) {
        setError(nameInput, '저장소 이름을 입력해주세요.');
        nameInput.focus();
        return;
    }
    if (!isMaxLength(name, 50)) {
        setError(nameInput, '저장소 이름은 최대 50자까지 입력 가능합니다.');
        nameInput.focus();
        return;
    }
    if (!isMaxLength(description, 255)) {
        setError(descriptionInput, '설명은 최대 255자까지 입력 가능합니다.');
        descriptionInput.focus();
        return;
    }

    apiFetch(`/api/core/storages/${storageId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: name, description: description === '' ? null : description })
    })
        .then(response => {
            if (response.ok) {
                closeEditModal();
                location.reload();
            } else {
                return response.json().then(err => alert(err.message || '수정에 실패했습니다.'));
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 통신 중 오류가 발생했습니다.');
        });
}

// 2. 저장소 상태 변경 (ACTIVE <-> INACTIVE)
function toggleStorageStatus(storageId, currentStatus) {
    const newStatus = (currentStatus === 'ACTIVE') ? 'INACTIVE' : 'ACTIVE';
    const actionText = (newStatus === 'ACTIVE') ? '활성화' : '비활성화';

    if (!confirm(`이 저장소를 ${actionText} 하시겠습니까?`)) return;

    apiFetch(`/api/core/storages/${storageId}/status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus })
    })
        .then(response => {
            if (response.ok) {
                location.reload();
            } else {
                return response.json().then(err => alert(err.message || '상태 변경에 실패했습니다.'));
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 통신 중 오류가 발생했습니다.');
        });
}

// 3. 저장소 삭제
function deleteStorage(storageId) {
    if (!confirm('정말 이 저장소를 삭제하시겠습니까?')) return;

    apiFetch(`/api/core/storages/${storageId}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (response.ok || response.status === 204) {
                alert('삭제되었습니다.');
                location.href = '/storages';
            } else {
                alert('삭제에 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 통신 중 오류가 발생했습니다.');
        });
}

// 4. 구역 추가
function createZone(storageId) {
    const nameInput = document.getElementById('zone-name');
    const descInput = document.getElementById('zone-description');

    const name = nameInput.value.trim();
    const description = descInput.value.trim();

    clearErrors(document.getElementById('zone-modal'));
    if (!isRequired(name)) {
        setError(nameInput, '구역 이름을 입력해야 합니다.');
        nameInput.focus();
        return;
    }
    if (!isMaxLength(name, 30)) {
        setError(nameInput, '구역 이름은 최대 30자 입니다.');
        nameInput.focus();
        return;
    }
    if (!isMaxLength(description, 255)) {
        setError(descInput, '구역 설명은 최대 255자 입니다.');
        descInput.focus();
        return;
    }

    apiFetch(`/api/core/storages/${storageId}/zones`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: name, description: description === '' ? null : description })
    })
        .then(response => {
            if (response.ok) {
                closeZoneModal();
                location.reload();
            } else {
                return response.json().then(err => alert(err.message || '구역 추가에 실패했습니다.'));
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 통신 중 오류가 발생했습니다.');
        });
}

// 최소 재고 임계값 수정
function updateStockThreshold(storageId, thresholdId) {
    const thresholdInput = document.getElementById(`input-threshold-${thresholdId}`);
    const activeCheckbox = document.getElementById(`input-active-${thresholdId}`);

    const requestData = {
        stockThreshold: parseInt(thresholdInput.value, 10),
        isActive: activeCheckbox.checked
    };

    if (isNaN(requestData.stockThreshold) || requestData.stockThreshold < 0) {
        alert('올바른 최소재고 숫자를 입력해주세요.');
        return;
    }

    apiFetch(`/api/core/storages/${storageId}/stock-thresholds/${thresholdId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
        .then(response => {
            if (response.ok) {
                // 수정 완료를 명확히 인지할 수 있도록 alert 추가
                alert('최소 재고 임계값이 성공적으로 수정되었습니다.');
                location.reload();
            } else {
                return response.json().then(err => {
                    alert(err.message || '수정에 실패했습니다.');
                }).catch(() => {
                    alert('수정에 실패했습니다.');
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 통신 중 오류가 발생했습니다.');
        });
}

// 최소 재고 임계값 삭제
function deleteStockThreshold(storageId, thresholdId) {
    if (!confirm('정말 이 최소 재고 임계값 설정을 삭제하시겠습니까?')) {
        return;
    }

    apiFetch(`/api/core/storages/${storageId}/stock-thresholds/${thresholdId}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (response.ok || response.status === 204) {
                alert('삭제되었습니다.');
                location.reload();
            } else {
                return response.json().then(err => {
                    alert(err.message || '삭제에 실패했습니다.');
                }).catch(() => {
                    alert('삭제에 실패했습니다.');
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 통신 중 오류가 발생했습니다.');
        });
}

// 최소 재고 추가 모달 열기/닫기
function openStockThresholdModal() {
    const modal = document.getElementById('stock-threshold-modal');
    clearErrors(modal);
    bootstrap.Modal.getOrCreateInstance(modal).show();
    document.getElementById('medicine-search-keyword').value = '';
    document.getElementById('medicine-search-result-body').innerHTML = '<tr><td colspan="3" style="text-align: center; color: #888; padding: 15px;">의약품을 검색해주세요.</td></tr>';
    document.getElementById('selected-package-unit-id').value = '';
    document.getElementById('selected-medicine-label').innerText = '없음';
    document.getElementById('new-stock-threshold').value = '';
}

function closeStockThresholdModal() {
    const modal = document.getElementById('stock-threshold-modal');
    clearErrors(modal);
    bootstrap.Modal.getOrCreateInstance(modal).hide();
}

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('hidden.bs.modal', () => {
            clearErrors(modal);
            if (modal.id === 'edit-modal') {
                resetEditForm();
            }
        });
    });
});

// 의약품 검색 API 호출
function searchMedicines() {
    const keywordInput = document.getElementById('medicine-search-keyword');
    const keyword = keywordInput.value.trim();
    clearErrors(document.getElementById('stock-threshold-modal'));

    if (!isRequired(keyword)) {
        setError(keywordInput, '검색어를 입력해주세요.');
        keywordInput.focus();
        return;
    }

    // SearchType.PRODUCT_NAME 기준 검색 요청 (필요시 page, size 조절 가능)
    apiFetch(`/api/core/medicines?searchType=PRODUCT_NAME&search=${encodeURIComponent(keyword)}&page=0&size=5`)
        .then(res => res.json())
        .then(resData => {
            // ApiResponse 구조에 맞게 content 배열 추출 (PageResponse 구조 고려)
            const pageData = resData.data;
            const medicines = pageData.content || [];

            const tbody = document.getElementById('medicine-search-result-body');
            tbody.innerHTML = '';

            if (medicines.length === 0) {
                tbody.innerHTML = '<tr><td colspan="3" style="text-align: center; color: #888; padding: 15px;">검색 결과가 없습니다.</td></tr>';
                return;
            }

            medicines.forEach(med => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                <td style="padding: 6px;">${med.productName} (${med.companyName || ''})</td>
                <td style="padding: 6px;">${med.packUnit}</td>
                <td style="padding: 6px; text-align: center;">
                    <button type="button" class="btn btn-sm btn-outline-primary"
                        onclick="selectMedicine('${med.packageUnitId}', '${med.productName} (${med.packUnit})')">선택</button>
                </td>
            `;
                tbody.appendChild(tr);
            });
        })
        .catch(err => {
            console.error('Error:', err);
            alert('의약품 검색 중 오류가 발생했습니다.');
        });
}

// 검색 결과에서 특정 의약품 선택
function selectMedicine(packageUnitId, displayName) {
    document.getElementById('selected-package-unit-id').value = packageUnitId;
    document.getElementById('selected-medicine-label').innerText = displayName;
}

// 최소 재고 임계값 저장 (POST)
function saveStockThreshold(storageId) {
    const medicinePackageUnitId = document.getElementById('selected-package-unit-id').value;
    const thresholdInput = document.getElementById('new-stock-threshold');
    const thresholdValue = thresholdInput.value.trim();
    const stockThreshold = parseInt(thresholdValue, 10);

    clearErrors(document.getElementById('stock-threshold-modal'));

    if (!medicinePackageUnitId) {
        alert('등록할 의약품을 선택해주세요.');
        return;
    }
    if (!isRequired(thresholdValue) || !isNumber(thresholdValue) || stockThreshold < 0) {
        setError(thresholdInput, '올바른 최소 재고 개수를 입력해주세요.');
        thresholdInput.focus();
        return;
    }

    const requestData = {
        medicinePackageUnitId: parseInt(medicinePackageUnitId, 10),
        stockThreshold: stockThreshold
    };

    apiFetch(`/api/core/storages/${storageId}/stock-thresholds`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
        .then(response => {
            if (response.ok || response.status === 201) {
                alert('최소 재고 임계값이 성공적으로 등록되었습니다.');
                location.reload();
            } else {
                return response.json().then(err => {
                    alert(err.message || '등록에 실패했습니다.');
                }).catch(() => {
                    alert('등록에 실패했습니다.');
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 통신 중 오류가 발생했습니다.');
        });
}
