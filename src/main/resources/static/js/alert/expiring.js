// /static/js/alert/expiring.js

document.addEventListener("DOMContentLoaded", function () {
    loadStorages(); // 저장소 목록 먼저 불러오기
    loadExpiringInventories(0);
});

// 저장소 목록 조회 후 드롭다운 채우기
function loadStorages() {
    fetch('/api/core/storages', {
        method: "GET",
        credentials: 'include',
        headers: {
            "Content-Type": "application/json"
        }
    })
        .then(response => response.json())
        .then(result => {
            const storages = result.data || result;
            const selectElement = document.getElementById("search-storage");

            if (Array.isArray(storages)) {
                storages.forEach(storage => {
                    const option = document.createElement("option");
                    option.value = storage.storageId;
                    // 예: [조직명] 저장소이름 형태로 표시
                    option.textContent = `[${storage.organizationName}] ${storage.name}`;
                    selectElement.appendChild(option);
                });
            }
        })
        .catch(error => {
            console.error("저장소 목록 로딩 실패:", error);
        });
}

// 유통기한 임박 재고 데이터 비동기 로드
function loadExpiringInventories(page) {
    const storageId = document.getElementById("search-storage").value;
    const filterType = document.getElementById("search-filter").value;
    const sortDirection = document.getElementById("search-sort").value;

    let url = `/api/core/expiring?page=${page}&size=20`;
    if (storageId) url += `&storageId=${storageId}`;
    if (filterType) url += `&filterType=${filterType}`;
    if (sortDirection) url += `&sortDirection=${sortDirection}`;

    fetch(url, {
        method: "GET",
        credentials: 'include',
        headers: {
            "Content-Type": "application/json"
        }
    })
        .then(response => response.json())
        .then(result => {
            const pageResponse = result.data || result;
            const content = pageResponse.content || [];

            renderTable(content, pageResponse.number || page, pageResponse.size || 20);
            renderPagination(pageResponse);
        })
        .catch(error => {
            console.error("데이터 로딩 실패:", error);
            document.getElementById("inventory-table-body").innerHTML =
                `<tr><td colspan="7" style="text-align: center; color: #dc2626; padding: 30px;">데이터를 불러오는 중 오류가 발생했습니다.</td></tr>`;
        });
}

// 테이블 렌더링
function renderTable(items, currentPage, size) {
    const tbody = document.getElementById("inventory-table-body");
    tbody.innerHTML = "";

    if (items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: #888; padding: 30px;">조건에 일치하는 재고가 없습니다.</td></tr>`;
        return;
    }

    items.forEach((item, index) => {
        const rowNumber = (currentPage * size) + (index + 1);

        const row = document.createElement("tr");
        row.innerHTML = `
            <td style="text-align: center; color: #64748b;">${rowNumber}</td>
            <td>${item.organizationName || '-'}</td>
            <td>${item.storageName || '-'} / <span style="color: #64748b;">${item.zoneName || '-'}</span></td>
            <td><strong>${item.medicineName}</strong> <span style="font-size: 0.8rem; color: #64748b;">(${item.packUnitName || ''})</span></td>
            <td><code>${item.lotNumber}</code></td>
            <td style="text-align: center;">${item.currentQuantity.toLocaleString()} 개</td>
            <td style="text-align: center;" class="text-danger">${item.expirationDate}</td>
        `;
        tbody.appendChild(row);
    });
}

// 페이지네이션 렌더링
function renderPagination(pageData) {
    const paginationContainer = document.getElementById("pagination");
    paginationContainer.innerHTML = "";

    const currentPage = pageData.number;
    const totalPages = pageData.totalPages;

    if (totalPages <= 1) return;

    const prevButton = document.createElement("button");
    prevButton.innerText = "이전";
    prevButton.disabled = currentPage === 0;
    prevButton.onclick = () => loadExpiringInventories(currentPage - 1);
    paginationContainer.appendChild(prevButton);

    for (let i = 0; i < totalPages; i++) {
        const pageButton = document.createElement("button");
        pageButton.innerText = i + 1;
        if (i === currentPage) {
            pageButton.classList.add("active");
        }
        pageButton.onclick = () => loadExpiringInventories(i);
        paginationContainer.appendChild(pageButton);
    }

    const nextButton = document.createElement("button");
    nextButton.innerText = "다음";
    nextButton.disabled = currentPage >= totalPages - 1;
    nextButton.onclick = () => loadExpiringInventories(currentPage + 1);
    paginationContainer.appendChild(nextButton);
}

// 검색 조건 초기화
function resetSearch() {
    document.getElementById("search-storage").value = "";
    document.getElementById("search-filter").value = "ALL";
    document.getElementById("search-sort").value = "ASC";
    loadExpiringInventories(0);
}