document.addEventListener("DOMContentLoaded", () => {
    loadStorages();
    loadUnderReviewInventories(0);

    document.getElementById("searchBtn").addEventListener("click", () => {
        loadUnderReviewInventories(0);
    });

    document.getElementById("submitReviewBtn").addEventListener("click", submitInventoryReview);
});

let currentSelectedInventoryId = null;
let currentPage = 0;

// --- 1. 저장소 목록 조회 (/api/core/inbound/storages) ---
function loadStorages() {
    apiFetch('/api/core/inbound/storages')
        .then(res => res.json())
        .then(result => {
            if (result.success && Array.isArray(result.data)) {
                const select = document.getElementById("storageSelect");
                result.data.forEach(storage => {
                    const option = document.createElement("option");
                    option.value = storage.storageId;
                    // 저장소 이름만 표시하도록 수정
                    option.textContent = storage.name;
                    select.appendChild(option);
                });
            }
        })
        .catch(err => console.error("저장소 목록 조회 실패:", err));
}

// --- 2. 검토 대상 재고 페이징 조회 (/api/core/inventories/under-reviews) ---
function loadUnderReviewInventories(page = 0) {
    currentPage = page;
    const storageId = document.getElementById("storageSelect").value;
    let url = `/api/core/inventories/under-reviews?page=${page}&size=20`;
    if (storageId) {
        url += `&storageId=${storageId}`;
    }

    apiFetch(url)
        .then(res => res.json())
        .then(result => {
            if (result.success && result.data) {
                const pageResponse = result.data;
                renderInventoryTable(pageResponse.content);
                renderPagination(pageResponse);
            }
        })
        .catch(err => console.error("재고 목록 조회 실패:", err));
}

function renderInventoryTable(items) {
    const tbody = document.getElementById("inventoryTableBody");
    tbody.innerHTML = "";

    // 데이터가 없을 때 명확하게 안내 문구 출력
    if (!items || items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-5">검토 대상 재고가 없습니다.</td></tr>`;
        return;
    }

    items.forEach(item => {
        const row = document.createElement("tr");
        const lastReviewFormatted = item.lastReviewAt ? item.lastReviewAt.replace('T', ' ') : '-';

        row.innerHTML = `
            <td>${item.productName}</td>
            <td>${item.packUnit}</td>
            <td>${item.lotNumber}</td>
            <td>${item.expirationDate}</td>
            <td>${item.currentQuantity}</td>
            <td>${item.storageName} / ${item.zoneName}</td>
            <td>${lastReviewFormatted}</td>
            <td>
                <button type="button" class="btn btn-primary btn-sm"
                    onclick="openEventModal(${item.inventoryId}, ${item.zoneId}, '${item.lastReviewAt || ''}')">
                    검토하기
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// --- 3. 페이징 UI 렌더링 (PageResponse 필드 활용) ---
function renderPagination(pageData) {
    const pagination = document.getElementById("pagination");
    pagination.innerHTML = "";

    const { page, totalPages, last } = pageData;

    if (totalPages <= 1) return; // 페이지가 1개 이하면 페이징 숨김

    // 이전 버튼
    const prevBtn = document.createElement("button");
    prevBtn.type = "button";
    prevBtn.textContent = "이전";
    prevBtn.className = "btn btn-outline-secondary btn-sm";
    prevBtn.disabled = page === 0;
    prevBtn.onclick = () => loadUnderReviewInventories(page - 1);
    pagination.appendChild(prevBtn);

    const pageInfo = document.createElement("span");
    pageInfo.className = "text-muted align-self-center px-2";
    pageInfo.textContent = `${page + 1} / ${totalPages}`;
    pagination.appendChild(pageInfo);

    const nextBtn = document.createElement("button");
    nextBtn.type = "button";
    nextBtn.textContent = "다음";
    nextBtn.className = "btn btn-outline-secondary btn-sm";
    nextBtn.disabled = last;
    nextBtn.onclick = () => loadUnderReviewInventories(page + 1);
    pagination.appendChild(nextBtn);
    nextBtn.onclick = () => loadUnderReviewInventories(page + 1);
    pagination.appendChild(nextBtn);
}

// --- 4. 모달 제어 및 이벤트 내역 조회 (/api/core/environment-events) ---
function openEventModal(inventoryId, zoneId, lastReviewAt) {
    currentSelectedInventoryId = inventoryId;

    document.getElementById("memoInput").value = "";
    document.querySelector('input[name="isOut"][value="false"]').checked = true;

    apiFetch(`/api/core/environment-events`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            zoneId: zoneId,
            lastReviewAt: lastReviewAt ? lastReviewAt : null
        })
    })
        .then(res => res.json())
        .then(result => {
            if (result.success) {
                renderEventTable(result.data);
                bootstrap.Modal
                    .getOrCreateInstance(document.getElementById("eventModal"))
                    .show();
            } else {
                alert('환경 이벤트 목록을 불러오지 못했습니다.');
            }
        })
        .catch(err => {
            console.error('환경 이벤트 조회 에러:', err);
            alert('환경 이벤트 조회 중 오류가 발생했습니다.');
        });
}

function closeEventModal() {
    bootstrap.Modal
        .getOrCreateInstance(document.getElementById("eventModal"))
        .hide();
}

function renderEventTable(events) {
    const tbody = document.getElementById("eventTableBody");
    tbody.innerHTML = "";

    if (!events || events.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4">발생한 환경 이벤트가 없습니다.</td></tr>`;
        return;
    }

    events.forEach(event => {
        const row = document.createElement("tr");
        row.innerHTML = `
                        <td>${event.environmentType}</td>
            <td>${event.breachType}</td>
            <td>${event.detectedValue}</td>
            <td>${event.thresholdValue}</td>
            <td>${event.createdAt.replace('T', ' ')}</td>
        `;
        tbody.appendChild(row);
    });
}

// --- 5. 최종 검토 완료 처리 요청 (/api/core/inventories/{inventory-id}/environment-reviews) ---
function submitInventoryReview() {
    const isOut = document.querySelector('input[name="isOut"]:checked').value === 'true';
    const memo = document.getElementById("memoInput").value.trim();

    apiFetch(`/api/core/inventories/${currentSelectedInventoryId}/environment-reviews`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            isOut: isOut,
            memo: memo || null
        })
    })
        .then(res => {
            if (res.ok) {
                alert('검토가 정상적으로 처리되었습니다.');
                closeEventModal();
                loadUnderReviewInventories(currentPage); // 현재 페이지 유지하며 목록 새로고침
            } else {
                alert('검토 처리에 실패했습니다.');
            }
        })
        .catch(err => {
            console.error('검토 처리 에러:', err);
            alert('서버 통신 중 오류가 발생했습니다.');
        });
}