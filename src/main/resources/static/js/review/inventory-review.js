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
                    option.textContent = `${storage.name} (${storage.organizationName})`;
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

    if (!items || items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: #888; padding: 20px;">검토 대상 재고가 없습니다.</td></tr>`;
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
                <button type="button" class="btn-primary" 
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
    prevBtn.textContent = "◀ 이전";
    prevBtn.className = "btn-secondary";
    prevBtn.disabled = page === 0;
    prevBtn.style.opacity = page === 0 ? "0.5" : "1";
    prevBtn.onclick = () => loadUnderReviewInventories(page - 1);
    pagination.appendChild(prevBtn);

    // 페이지 번호 표시 (간단히 현재 페이지 / 전체 페이지 표시 또는 번호 나열)
    const pageInfo = document.createElement("span");
    pageInfo.style.alignSelf = "center";
    pageInfo.style.padding = "0 10px";
    pageInfo.textContent = `${page + 1} / ${totalPages}`;
    pagination.appendChild(pageInfo);

    // 다음 버튼
    const nextBtn = document.createElement("button");
    nextBtn.type = "button";
    nextBtn.textContent = "다음 ▶";
    nextBtn.className = "btn-secondary";
    nextBtn.disabled = last;
    nextBtn.style.opacity = last ? "0.5" : "1";
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
                document.getElementById('eventModal').style.display = 'block';
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
    document.getElementById('eventModal').style.display = 'none';
}

function renderEventTable(events) {
    const tbody = document.getElementById("eventTableBody");
    tbody.innerHTML = "";

    if (!events || events.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: #888; padding: 15px;">발생한 환경 이벤트가 없습니다.</td></tr>`;
        return;
    }

    events.forEach(event => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td style="padding: 10px; border-bottom: 1px solid #dee2e6; font-size: 13px;">${event.environmentType}</td>
            <td style="padding: 10px; border-bottom: 1px solid #dee2e6; font-size: 13px;">${event.breachType}</td>
            <td style="padding: 10px; border-bottom: 1px solid #dee2e6; font-size: 13px;">${event.detectedValue}</td>
            <td style="padding: 10px; border-bottom: 1px solid #dee2e6; font-size: 13px;">${event.thresholdValue}</td>
            <td style="padding: 10px; border-bottom: 1px solid #dee2e6; font-size: 13px;">${event.createdAt.replace('T', ' ')}</td>
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