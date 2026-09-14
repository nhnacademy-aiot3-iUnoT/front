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

    if (!items || items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-5">검토 대상 재고가 없습니다.</td></tr>`;
        return;
    }

    items.forEach(item => {
        const row = document.createElement("tr");
        const lastReviewDate = item.lastReviewAt ? item.lastReviewAt.slice(0, 10) : '-';
        const lastReviewTime = item.lastReviewAt ? item.lastReviewAt.slice(11, 19) : '';

            row.innerHTML = `
            <td>
            <strong class="d-block">${item.productName}</strong>
        <small class="d-block text-secondary">${item.packUnit}</small>
    </td>

        <td class="text-nowrap">${item.lotNumber}</td>

        <td class="text-nowrap">${item.expirationDate}</td>

        <td>
            <strong class="d-block fw-normal text-nowrap">
                ${item.storageName}
            </strong>
            <small class="d-block text-secondary text-nowrap">
                ${item.zoneName}
            </small>
        </td>

        <td class="text-nowrap">${item.currentQuantity}</td>

        <td>
            <span class="d-block text-nowrap">${lastReviewDate}</span>
            <small class="d-block text-secondary text-nowrap">
                ${lastReviewTime}
            </small>
        </td>

        <td class="text-end text-nowrap">
            <button type="button"
                    class="btn btn-primary btn-sm"
                    onclick="openEventModal(${item.inventoryId}, ${item.zoneId}, '${item.lastReviewAt || ''}', ${item.medicinePackageUnitId})">
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

    if (totalPages <= 1) return;

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
}

// --- 4. 모달 제어 및 환경 기준/이벤트 내역 조회 ---
function openEventModal(inventoryId, zoneId, lastReviewAt, medicinePackageUnitId) {
    currentSelectedInventoryId = inventoryId;

    document.getElementById("memoInput").value = "";
    document.querySelector('input[name="isOut"][value="false"]').checked = true;

    // 1. 해당 의약품의 환경 기준치 조회 및 렌더링
    loadEnvironmentStandards(medicinePackageUnitId);

    // 2. 환경 이벤트 내역 조회 파라미터 설정
    const params = new URLSearchParams({
        zoneId: zoneId
    });

    if (lastReviewAt && lastReviewAt !== 'null' && lastReviewAt !== 'undefined') {
        params.append('lastReviewAt', lastReviewAt);
    }

    apiFetch(`/api/core/environment-events?${params.toString()}`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
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

// 환경 기준치 조회 함수
function loadEnvironmentStandards(medicinePackageUnitId) {
    const tbody = document.getElementById("environmentStandardTableBody");
    tbody.innerHTML = `<tr><td colspan="3" class="text-center text-muted py-2">기준 정보를 불러오는 중...</td></tr>`;

    apiFetch(`/api/core/package-units/${medicinePackageUnitId}/medicine-environment-types`)
        .then(res => res.json())
        .then(result => {
            if (result.success && Array.isArray(result.data)) {
                renderEnvironmentStandardTable(result.data);
            } else {
                tbody.innerHTML = `<tr><td colspan="3" class="text-center text-muted py-2">설정된 환경 기준이 없습니다.</td></tr>`;
            }
        })
        .catch(err => {
            console.error("환경 기준 조회 실패:", err);
            tbody.innerHTML = `<tr><td colspan="3" class="text-center text-danger py-2">기준 조회 실패</td></tr>`;
        });
}

// 환경 기준치 테이블 렌더링 함수
function renderEnvironmentStandardTable(standards) {
    const tbody = document.getElementById("environmentStandardTableBody");
    tbody.innerHTML = "";

    if (!standards || standards.length === 0) {
        tbody.innerHTML = `<tr><td colspan="3" class="text-center text-muted py-3">이 의약품에 설정된 환경 기준이 없습니다.</td></tr>`;
        return;
    }

    standards.forEach(std => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${std.type}</td>
            <td>${std.min !== null && std.min !== undefined ? std.min : '-'}</td>
            <td>${std.max !== null && std.max !== undefined ? std.max : '-'}</td>
        `;
        tbody.appendChild(row);
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
                loadUnderReviewInventories(currentPage);
            } else {
                alert('검토 처리에 실패했습니다.');
            }
        })
        .catch(err => {
            console.error('검토 처리 에러:', err);
            alert('서버 통신 중 오류가 발생했습니다.');
        });
}
