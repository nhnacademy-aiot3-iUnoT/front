// 모달 열기
function openCreateModal() {
    document.getElementById('create-modal').style.display = 'flex';
    document.getElementById('new-name').focus();
}

// 모달 닫기
function closeCreateModal() {
    document.getElementById('create-modal').style.display = 'none';
    document.getElementById('new-name').value = '';
    document.getElementById('new-description').value = '';
}

// 저장소 생성 API 호출 함수
function createStorage() {
    const nameInput = document.getElementById('new-name');
    const descInput = document.getElementById('new-description');

    const name = nameInput.value.trim();
    const description = descInput.value.trim();

    // 클라이언트단 유효성 검사 (@NotBlank, @Size 대응)
    if (!name) {
        alert('저장소 이름을 입력해주세요.');
        nameInput.focus();
        return;
    }
    if (name.length > 50) {
        alert('저장소 이름은 최대 50자까지 입력 가능합니다.');
        nameInput.focus();
        return;
    }
    if (description.length > 255) {
        alert('설명은 최대 255자까지 입력 가능합니다.');
        descInput.focus();
        return;
    }

    fetch(`/api/core/storages`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
            name: name,
            description: description === '' ? null : description
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

// 💡 [추가됨] 모달 박스 내부를 클릭했을 때 배경으로 클릭 이벤트가 새어나가는 것 방지
document.addEventListener("DOMContentLoaded", function() {
    const modalBox = document.querySelector("#create-modal .modal-box");
    if (modalBox) {
        modalBox.addEventListener("click", function(event) {
            event.stopPropagation();
        });
    }
});