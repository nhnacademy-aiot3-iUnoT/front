// 모달 열기
function openCreateModal() {
    const modal = document.getElementById('create-modal');
    clearErrors(modal);
    bootstrap.Modal.getOrCreateInstance(modal).show();
    document.getElementById('new-name').focus();

    loadDepartmentsForModal();
}

// 모달 닫기
function closeCreateModal() {
    const modal = document.getElementById('create-modal');
    clearErrors(modal);
    bootstrap.Modal.getOrCreateInstance(modal).hide();
    document.getElementById('new-name').value = '';
    document.getElementById('new-description').value = '';
    document.getElementById('department-checkbox-container').innerHTML = '';
}

document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('create-modal');
    modal?.addEventListener('hidden.bs.modal', () => {
        clearErrors(modal);
        document.getElementById('new-name').value = '';
        document.getElementById('new-description').value = '';
    });
});

function loadDepartmentsForModal() {
    const container = document.getElementById('department-checkbox-container');
    container.innerHTML = '<span style="color: #888; font-size: 13px;">부서 목록을 불러오는 중...</span>';

    apiFetch(`/api/core/departments`, {
        method: 'GET'
    })
        .then(response => {
            if (!response.ok) throw new Error('부서 목록을 불러오지 못했습니다.');
            return response.json();
        })
        .then(result => {
            container.innerHTML = '';

            const departments = result.data || [];

            if (!departments || departments.length === 0) {
                container.innerHTML = '<span style="color: #888; font-size: 13px;">등록된 부서가 없습니다.</span>';
                return;
            }

            departments.forEach(dept => {
                const label = document.createElement('label');

                const checkbox = document.createElement('input');
                checkbox.type = 'checkbox';
                checkbox.name = 'departmentIds';
                checkbox.value = dept.id;
                checkbox.style.marginRight = '6px';

                label.appendChild(checkbox);
                label.append(`${dept.name}`);

                container.appendChild(label);
            });
        })
        .catch(error => {
            console.error('Error:', error);
            container.innerHTML = '<span style="color: red; font-size: 13px;">부서 목록 로드 실패</span>';
        });
}

// 저장소 생성 API 호출 함수
function createStorage() {
    const nameInput = document.getElementById('new-name');
    const descInput = document.getElementById('new-description');

    const name = nameInput.value.trim();
    const description = descInput.value.trim();

    const selectedDepartments = Array.from(
        document.querySelectorAll('input[name="departmentIds"]:checked')
    ).map(cb => Number(cb.value));

    clearErrors(document.getElementById('create-modal'));

    // 클라이언트단 유효성 검사 (@NotBlank, @Size 대응)
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
        setError(descInput, '설명은 최대 255자까지 입력 가능합니다.');
        descInput.focus();
        return;
    }

    apiFetch(`/api/core/storages`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            name: name,
            description: description === '' ? null : description,
            departmentIds: selectedDepartments.length > 0 ? selectedDepartments : null
        })
    })
        .then(response => {
            if (response.ok) {
                closeCreateModal();
                location.reload(); // 성공 시 새로고침하여 목록 반영
            } else {
                return response.json().then(err => {
                    alert(err.message || '저장소 추가에 실패했습니다.');
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 통신 중 오류가 발생했습니다.');
        });
}
