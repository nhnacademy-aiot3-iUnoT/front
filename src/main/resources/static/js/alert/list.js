let currentPage = 0; // 현재 페이지 번호 기억용
const canManage = document.body.dataset.canManage === 'true';

document.addEventListener('DOMContentLoaded', () => {
    // 최초 진입 시 0페이지 조회
    loadAlerts(0);
});

// 1. 알림 리스트 조회 및 렌더링
function loadAlerts(page = 0) {
    currentPage = page;
    const alertType = document.getElementById('filter-alert-type').value;
    const isChecked = document.getElementById('filter-is-checked').value;

    let url = `/api/core/alerts?page=${page}&`;
    if (alertType) url += `alertType=${alertType}&`;
    if (isChecked !== '') url += `isChecked=${isChecked}&`;

    apiFetch(url, {
        method: 'GET'
    })
        .then(res => res.json())
        .then(resData => {
            const pageResponse = resData.data;
            const alerts = pageResponse.content;
            const tbody = document.getElementById('alert-table-body');

            tbody.innerHTML = '';

            if (!alerts || alerts.length === 0) {
                tbody.innerHTML = `<tr><td colspan="6" class="text-center text-secondary py-5">조건에 해당하는 알림이 없습니다.</td></tr>`;
                document.getElementById('pagination').innerHTML = '';
                return;
            }

            alerts.forEach(item => {
                const tr = document.createElement('tr');
                if (!item.isChecked) {
                    tr.classList.add('fw-bold', 'table-active');
                }

                tr.innerHTML = `
                <td class="text-center"><input type="checkbox" class="form-check-input alert-checkbox" value="${item.alertId}" aria-label="알림 선택"></td>
                <td><strong class="department-name">조회 중...</strong></td>
                <td><span class="badge status-badge status-warning">${formatAlertType(item.alertType)}</span></td>
                <td class="alert-message"></td>
                <td class="text-center text-secondary small">${formatDate(item.createdAt)}</td>
                <td class="text-center">
                    <span class="badge status-badge ${item.isChecked ? 'status-inactive' : 'status-critical'}">
                        ${item.isChecked ? '읽음' : '안읽음'}
                    </span>
                </td>
            `;
                const messageCell = tr.querySelector('.alert-message');

                const targetUrl = {
                    ENV_WARNING: '/inventories/under-reviews',
                    EXPIRING: '/expiring'
                }[item.alertType];

                if (targetUrl) {
                    const link = document.createElement('a');
                    link.href = targetUrl;
                    link.textContent = item.message;
                    link.style.cursor = 'pointer';
                    messageCell.appendChild(link);
                } else {
                    messageCell.textContent = item.message;
                }
                getDepartmentNames(item.message)
                    .then(names => {
                        tr.querySelector('.department-name').textContent = names;
                    })
                    .catch(() => {
                        tr.querySelector('.department-name').textContent = '-';
                    });
                tbody.appendChild(tr);
            });

            // 페이지네이션 렌더링 호출
            renderPagination(pageResponse);
        })
        .catch(err => {
            console.error('알림 조회 실패:', err);
        });
}
let myDepartmentIdsPromise;
const departmentNamesByStorage = new Map();

async function getMyDepartmentIds() {
    if (!myDepartmentIdsPromise) {
        const departmentUrl = canManage
            ? '/api/core/departments'
            : '/api/core/departments/me';

        myDepartmentIdsPromise = apiFetch(departmentUrl, {
            method: 'GET'
        })
            .then(response => response.json())
            .then(result => new Set(
                (result.data || []).map(department => department.id)
            ));
    }

    return myDepartmentIdsPromise;
}

async function getDepartmentNames(message) {
    const storageName =
        message?.match(/^\[([^\]]+)]/)?.[1]?.trim()
        ?? message?.match(/^저장소:\s*(.+?)\s+의약품:/)?.[1]?.trim();

    if (!storageName) return '-';

    if (!departmentNamesByStorage.has(storageName)) {
        departmentNamesByStorage.set(storageName, (async () => {
            const [storageResponse, myDepartmentIds] = await Promise.all([
                apiFetch(
                    `/api/core/storages?name=${encodeURIComponent(storageName)}`,
                    { method: 'GET' }
                ),
                getMyDepartmentIds()
            ]);

            const storageData = await storageResponse.json();
            const storage = (storageData.data || [])
                .find(item => item.name === storageName);

            if (!storage) return '-';

            const departmentResponse = await apiFetch(
                `/api/core/storages/${storage.storageId}/departments`,
                { method: 'GET' }
            );
            const departmentData = await departmentResponse.json();

            return (departmentData.data || [])
                .filter(department =>
                    myDepartmentIds.has(department.departmentId)
                )
                .map(department => department.name)
                .join(', ') || '-';
        })());
    }

    return departmentNamesByStorage.get(storageName);
}

// 페이지네이션 버튼 동적 생성 로직
function renderPagination(pageData) {
    const paginationEl = document.getElementById('pagination');
    paginationEl.innerHTML = '';

    const totalPages = pageData.totalPages;
    if (totalPages <= 1) return; // 페이지가 1개 이하면 숨김

    const current = pageData.number; // 현재 페이지 (0부터 시작)

    // 이전 버튼
    if (!pageData.first) {
        const prevBtn = createPageButton('‹ 이전', current - 1);
        paginationEl.appendChild(prevBtn);
    }

    // 페이지 번호 버튼들
    for (let i = 0; i < totalPages; i++) {
        const pageBtn = createPageButton(i + 1, i);
        if (i === current) {
            pageBtn.classList.add('active');
        }
        paginationEl.appendChild(pageBtn);
    }

    // 다음 버튼
    if (!pageData.last) {
        const nextBtn = createPageButton('다음 ›', current + 1);
        paginationEl.appendChild(nextBtn);
    }
}

// 페이지 버튼 생성 헬퍼
function createPageButton(text, targetPage) {
    const item = document.createElement('li');
    item.className = 'page-item';
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.innerText = text;
    btn.className = 'page-link';
    btn.onclick = () => loadAlerts(targetPage);
    item.appendChild(btn);
    return item;
}

// 타입 한글 변환 헬퍼
function formatAlertType(type) {
    switch(type) {
        case 'LOW_STOCK': return '재고 부족';
        case 'ENV_WARNING': return '환경 경고';
        case 'EXPIRING': return '유통기한 임박';
        default: return type;
    }
}

// 날짜 포맷 헬퍼
function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}

// 전체 선택 체크박스 제어
function toggleSelectAll(master) {
    const checkboxes = document.querySelectorAll('.alert-checkbox');
    checkboxes.forEach(cb => cb.checked = master.checked);
}

// 2. 선택 읽음 처리
function markSelectedAsChecked() {
    const selectedIds = getSelectedAlertIds();
    if (selectedIds.length === 0) {
        alert('선택된 알림이 없습니다.'); // 여기서 쓰는 alert()은 브라우저 내장함수라 정상 동작합니다!
        return;
    }

    apiFetch(`/api/core/alerts/check`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ alertIds: selectedIds })
    })
        .then(res => {
            if (res.ok) {
                loadAlerts(currentPage);
                if (typeof fetchUnreadAlertCount === 'function') fetchUnreadAlertCount();
            } else {
                alert('읽음 처리 중 오류가 발생했습니다.');
            }
        });
}

// 3. 선택 삭제
function deleteSelectedAlerts() {
    const selectedIds = getSelectedAlertIds();
    if (selectedIds.length === 0) {
        alert('삭제할 알림을 선택해주세요.');
        return;
    }

    if (!confirm('선택한 알림을 정말 삭제하시겠습니까?')) return;

    apiFetch(`/api/core/alerts`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ alertIds: selectedIds })
    })
        .then(res => {
            if (res.ok) {
                loadAlerts(currentPage);
                if (typeof fetchUnreadAlertCount === 'function') fetchUnreadAlertCount();
            } else {
                alert('삭제 중 오류가 발생했습니다.');
            }
        });
}

// 4. 전체 삭제
function deleteAllAlerts() {
    if (!confirm('모든 알림을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) return;

    apiFetch(`/api/core/alerts/all`, {
        method: 'DELETE'
    })
        .then(res => {
            if (res.ok) {
                loadAlerts(0);
                if (typeof fetchUnreadAlertCount === 'function') fetchUnreadAlertCount();
            } else {
                alert('전체 삭제 중 오류가 발생했습니다.');
            }
        });
}

// 체크된 알림 ID 수집 유틸
function getSelectedAlertIds() {
    const checkboxes = document.querySelectorAll('.alert-checkbox:checked');
    return Array.from(checkboxes).map(cb => Number(cb.value));
}
