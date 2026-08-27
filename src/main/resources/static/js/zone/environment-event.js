document.addEventListener("DOMContentLoaded", () => {
    fetchEnvironmentEvents(0);
});

// --- API 로직 ---
function fetchEnvironmentEvents(page) {
    const zoneId = document.getElementById("zone-id").value;
    const envType = document.getElementById("search-env-type").value;
    const breachType = document.getElementById("search-breach-type").value;

    let url = `/api/core/zones/${zoneId}/environment-events?page=${page}&size=10&sort=createdAt,desc`;

    if (envType) {
        url += `&environmentType=${envType}`;
    }
    if (breachType) {
        url += `&breachType=${breachType}`;
    }

    apiFetch(url, {
        method: 'GET'
    })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                renderTable(result.data.content);
                renderPagination(result.data);
            } else {
                alert("이벤트 내역을 불러오는데 실패했습니다.");
            }
        })
        .catch(error => {
            console.error("Error:", error);
            alert("오류가 발생했습니다.");
        });
}

// --- 렌더링 로직 ---
function renderTable(contentList) {
    const tbody = document.getElementById("event-table-body");
    tbody.innerHTML = "";

    if (!contentList || contentList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #888; padding: 30px;">조회된 이벤트 내역이 없습니다.</td></tr>`;
        return;
    }

    contentList.forEach(item => {
        const tr = document.createElement("tr");

        let typeBadgeClass = "";
        if (item.environmentType === 'TEMPERATURE') typeBadgeClass = "type-temp";
        else if (item.environmentType === 'HUMIDITY') typeBadgeClass = "type-humi";
        else typeBadgeClass = "type-illum";

        tr.innerHTML = `
            <td>${item.environmentEventId}</td>
            <td><span class="badge-type ${typeBadgeClass}">${item.environmentType}</span></td>
            <td><strong>${item.breachType}</strong></td>
            <td>${item.detectedValue}</td>
            <td>${item.thresholdValue}</td>
            <td>${item.organizationName} > ${item.storageName} > ${item.zoneName}</td>
        `;
        tbody.appendChild(tr);
    });
}

function renderPagination(pageData) {
    const paginationBox = document.getElementById("pagination-box");
    paginationBox.innerHTML = "";

    const totalPages = pageData.totalPages;
    const currentPage = pageData.number; // 0-based

    if (totalPages <= 1) return;

    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement("button");
        btn.innerText = i + 1;
        if (i === currentPage) {
            btn.classList.add("active");
        }
        btn.onclick = () => fetchEnvironmentEvents(i);
        paginationBox.appendChild(btn);
    }
}