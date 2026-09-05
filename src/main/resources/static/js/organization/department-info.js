// 추후 확인 필요
document.addEventListener('DOMContentLoaded', () => {
    const MIN_SEARCH_LENGTH = 2;
    const SEARCH_DELAY = 400;
    const departmentId = window.departmentId;
    const assignedMemberIds = new Set(window.assignedMemberIds.map(String));
    const storageSearch = document.querySelector('#storage-search');
    const storageResults = document.querySelector('#storage-search-results');
    const memberSearch = document.querySelector('#member-search');
    const memberResults = document.querySelector('#member-search-results');

    setupTelegramChat(departmentId);

    if (!storageSearch || !memberSearch) {
        return;
    }

    document.querySelectorAll('[data-storage-remove]').forEach((button) => {
        button.addEventListener('click', () => removeRelation(
            `/departments/${departmentId}/storages/${button.dataset.storageId}`,
            '저장소'
        ));
    });

    document.querySelectorAll('[data-member-remove]').forEach((button) => {
        button.addEventListener('click', () => removeRelation(
            `/departments/${departmentId}/members/${button.dataset.memberId}`,
            '조직원'
        ));
    });

    let storageTimer;
    let memberTimer;
    let storageController;
    let memberController;

    async function removeRelation(url, target) {
        const response = await fetch(url, {method: 'DELETE'});
        if (!response.ok) {
            alert(`${target} 삭제에 실패했습니다.`);
            return;
        }
        window.location.reload();
    }

    storageSearch.addEventListener('input', () => {
        clearTimeout(storageTimer);
        storageController?.abort();

        const keyword = storageSearch.value.trim();
        if (keyword.length < MIN_SEARCH_LENGTH) {
            storageResults.replaceChildren();
            return;
        }

        storageTimer = setTimeout(async () => {
            storageController = new AbortController();
            try {
                const response = await fetch(
                    `/organizations/search/storages?name=${encodeURIComponent(keyword)}`,
                    { signal: storageController.signal }
                );
                if (!response.ok) {
                    throw new Error();
                }
                const storages = await response.json();
                storageResults.replaceChildren(...storages.map((storage) => createStorageResult(storage)));
            } catch (error) {
                if (error.name !== 'AbortError') {
                    storageResults.textContent = '저장소 검색에 실패했습니다.';
                }
            }
        }, SEARCH_DELAY);
    });

    memberSearch.addEventListener('input', () => {
        clearTimeout(memberTimer);
        memberController?.abort();

        const keyword = memberSearch.value.trim();
        if (keyword.length < MIN_SEARCH_LENGTH) {
            memberResults.replaceChildren();
            return;
        }

        memberTimer = setTimeout(async () => {
            memberController = new AbortController();
            try {
                const response = await fetch(
                    `/organizations/search/members?email=${encodeURIComponent(keyword)}`,
                    { signal: memberController.signal }
                );
                if (!response.ok) {
                    throw new Error();
                }
                const members = await response.json();
                memberResults.replaceChildren(
                    ...members.map((member) => createMemberResult(member))
                );
            } catch (error) {
                if (error.name !== 'AbortError') {
                    memberResults.textContent = '조직원 검색에 실패했습니다.';
                }
            }
        }, SEARCH_DELAY);
    });

    function createMemberResult(member) {
        const row = document.createElement('div');
        row.className = 'list-group-item d-flex justify-content-between align-items-center';
        const assigned = assignedMemberIds.has(String(member.memberId));
        row.innerHTML = `<span>${member.email}</span><button class="btn btn-sm ${assigned ? 'btn-outline-danger' : 'btn-outline-primary'}">${assigned ? '삭제' : '추가'}</button>`;
        row.querySelector('button').addEventListener('click', async () => {
            const method = assigned ? 'DELETE' : 'POST';
            const response = await fetch(`/departments/${departmentId}/members/${member.memberId}`, { method });
            if (!response.ok) {
                throw new Error('조직원 추가에 실패했습니다.');
            }
            window.location.reload();
        });
        return row;
    }

    async function errorMessage(response, fallback) {
        try {
            const body = await response.json();
            return body.message || fallback;
        } catch (error) {
            return fallback;
        }
    }

    function setupTelegramChat(departmentId) {
        const chatIdInput = document.querySelector('#telegram-chat-id');
        const enabledInput = document.querySelector('#telegram-enabled');
        const saveButton = document.querySelector('#telegram-save');
        const unlinkButton = document.querySelector('#telegram-unlink');

        if (!chatIdInput || !saveButton) {
            return;
        }

        const url = `/departments/${departmentId}/telegram-chat`;

        saveButton.addEventListener('click', async () => {
            const chatId = chatIdInput.value.trim();

            chatIdInput.classList.remove('is-invalid');
            if (!chatId) {
                chatIdInput.classList.add('is-invalid');
                chatIdInput.parentElement.querySelector('.invalid-feedback').textContent =
                    '단톡방 번호를 입력해주세요.';
                return;
            }

            saveButton.disabled = true;
            try {
                const response = await fetch(url, {
                    method: 'PUT',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({chatId, enabled: enabledInput.checked})
                });

                if (!response.ok) {
                    alert(await errorMessage(response, '단톡방 연결에 실패했습니다.'));
                    return;
                }

                window.location.reload();
            } finally {
                saveButton.disabled = false;
            }
        });

        if (unlinkButton) {
            unlinkButton.addEventListener('click', async () => {
                if (!confirm('단톡방 연결을 해제하시겠습니까? 해당 방에서 챗봇이 동작하지 않습니다.')) {
                    return;
                }

                const response = await fetch(url, {method: 'DELETE'});
                if (!response.ok) {
                    alert(await errorMessage(response, '단톡방 연결 해제에 실패했습니다.'));
                    return;
                }

                window.location.reload();
            });
        }
    }

    function createStorageResult(storage) {
        const row = document.createElement('div');
        row.className = 'list-group-item d-flex justify-content-between align-items-center';
        const assigned = document.querySelector(
            `[data-assigned-storage-id="${storage.storageId}"]`
        ) !== null;
        row.innerHTML = `<span>${storage.name}</span><button class="btn btn-sm ${assigned ? 'btn-outline-danger' : 'btn-outline-primary'}">${assigned ? '삭제' : '추가'}</button>`;
        row.querySelector('button').addEventListener('click', async () => {
            const method = assigned ? 'DELETE' : 'POST';
            const response = await fetch(`/departments/${departmentId}/storages/${storage.storageId}`, { method });
            if (!response.ok) {
                throw new Error('저장소 추가에 실패했습니다.');
            }
            window.location.reload();
        });
        return row;
    }
});
