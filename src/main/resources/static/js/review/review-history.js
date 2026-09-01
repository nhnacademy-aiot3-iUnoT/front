document.addEventListener("DOMContentLoaded", () => {
    loadStorages();
    loadReviewHistories(0);

    document.getElementById("searchBtn").addEventListener("click", () => {
        loadReviewHistories(0);
    });
});

let currentPage = 0;

// --- 1. 저장소 목록 조회 ---
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

// --- 2. 검토 내역 페이징 조회 ---
function loadReviewHistories(page = 0) {
    currentPage = page;
    const storageId = document.getElementById("storageSelect").value;
    let url = `/api/core/environment-reviews?page=${page}&size=20`;
    if (storageId) {
        url += `&storageId=${storageId}`;
    }

    apiFetch(url)
        .then(res => res.json())
        .then(result => {
            if (result.success && result.data) {
                const pageResponse = result.data;
                renderHistoryTable(pageResponse.content);
                renderPagination(pageResponse);
            }
        })
        .catch(err => console.error("검토 내역 조회 실패:", err));
}

function renderHistoryTable(items) {
    const tbody = document.getElementById("historyTableBody");
    tbody.innerHTML = "";

    if (!items || items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #888; padding: 30px;">검토 내역이 없습니다.</td></tr>`;
        return;
    }

    items.forEach(item => {
        const row = document.createElement("tr");
        const createdAtFormatted = item.createdAt ? item.createdAt.replace('T', ' ') : '-';
        const statusBadge = item.isOut
            ? `<span style="color: #d9534f; font-weight: bold;">폐기 처리</span>`
            : `<span style="color: #5cb85c; font-weight: bold;">정상 처리</span>`;

        row.innerHTML = `
            <td>${item.productName}</td>
            <td>${item.packUnit}</td>
            <td>${statusBadge}</td>
            <td>${createdAtFormatted}</td>
            <td>${item.reviewerId}</td>
            <td>
                <button type="button" class="btn-secondary" onclick="location.href='/environment-reviews/${item.environmentReviewId}'">
                    상세보기
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// --- 3. 페이징 UI 렌더링 ---
function renderPagination(pageData) {
    const pagination = document.getElementById("pagination");
    pagination.innerHTML = "";

    const { page, totalPages, last } = pageData;
    if (totalPages <= 1) return;

    const prevBtn = document.createElement("button");
    prevBtn.type = "button";
    prevBtn.textContent = "◀ 이전";
    prevBtn.className = "btn-secondary";
    prevBtn.disabled = page === 0;
    prevBtn.style.opacity = page === 0 ? "0.5" : "1";
    prevBtn.onclick = () => loadReviewHistories(page - 1);
    pagination.appendChild(prevBtn);

    const pageInfo = document.createElement("span");
    pageInfo.style.alignSelf = "center";
    pageInfo.style.padding = "0 10px";
    pageInfo.textContent = `${page + 1} / ${totalPages}`;
    pagination.appendChild(pageInfo);

    const nextBtn = document.createElement("button");
    nextBtn.type = "button";
    nextBtn.textContent = "다음 ▶";
    nextBtn.className = "btn-secondary";
    nextBtn.disabled = last;
    nextBtn.style.opacity = last ? "0.5" : "1";
    nextBtn.onclick = () => loadReviewHistories(page + 1);
    pagination.appendChild(nextBtn);
}