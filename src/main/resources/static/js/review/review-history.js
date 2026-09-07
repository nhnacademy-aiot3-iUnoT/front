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
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-secondary py-5">
                    검토 내역이 없습니다.
                </td>
            </tr>
        `;
        return;
    }

    items.forEach(item => {
        const row = document.createElement("tr");
        const createdAtFormatted =
            item.createdAt ? item.createdAt.replace("T", " ") : "-";

        const statusBadge = item.isOut
            ? `<span class="badge bg-danger-lt text-danger">폐기 처리</span>`
            : `<span class="badge bg-success-lt text-success">정상 처리</span>`;

        row.innerHTML = `
            <td class="fw-medium">${item.productName}</td>
            <td class="text-secondary">${item.packUnit}</td>
            <td>${statusBadge}</td>
            <td class="text-secondary">${createdAtFormatted}</td>
            <td class="text-secondary">${item.reviewerName}</td>
            <td class="text-end">
                <button type="button"
                        class="btn btn-sm btn-outline-secondary"
                        onclick="location.href='/environment-reviews/${item.environmentReviewId}'">
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

    if (totalPages <= 1) {
        return;
    }

    const appendControl = (label, disabled, onClick) => {
        const item = document.createElement("li");
        item.className = `page-item${disabled ? " disabled" : ""}`;

        const button = document.createElement("button");
        button.type = "button";
        button.className = "page-link";
        button.textContent = label;
        button.disabled = disabled;
        button.onclick = onClick;

        item.appendChild(button);
        pagination.appendChild(item);
    };

    appendControl("이전", page === 0, () => loadReviewHistories(page - 1));

    const pageItem = document.createElement("li");
    pageItem.className = "page-item disabled";

    const pageInfo = document.createElement("span");
    pageInfo.className = "page-link";
    pageInfo.textContent = `${page + 1} / ${totalPages}`;

    pageItem.appendChild(pageInfo);
    pagination.appendChild(pageItem);

    appendControl("다음", last, () => loadReviewHistories(page + 1));
}