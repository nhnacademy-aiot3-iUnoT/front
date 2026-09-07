// /static/js/alert/expiring.js

const selectedInventories = new Map();

let visibleInventories = [];
let loadedPage = 0;

document.addEventListener("DOMContentLoaded", function () {
    const selectAllCheckbox =
        document.getElementById("select-all-expired");

    const disposeSelectedButton =
        document.getElementById("dispose-selected-button");

    const confirmDisposalButton =
        document.getElementById(
            "confirm-selected-disposal-button"
        );

    selectAllCheckbox.addEventListener("change", event => {
        selectVisibleExpiredInventories(event.target.checked);
    });

    disposeSelectedButton.addEventListener("click", () => {
        openSelectedDisposalModal();
    });

    confirmDisposalButton.addEventListener("click", () => {
        disposeSelectedInventories();
    });

    loadStorages();
    loadExpiringInventories(0);
});

async function loadStorages() {
    try {
        const response = await apiFetch("/api/core/storages", {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error(
                await getErrorMessage(
                    response,
                    "저장소 목록을 불러오지 못했습니다."
                )
            );
        }

        const result = await response.json();
        const storages = result.data || result;
        const selectElement =
            document.getElementById("search-storage");

        if (!Array.isArray(storages)) {
            return;
        }

        storages.forEach(storage => {
            const option = document.createElement("option");

            option.value = storage.storageId;
            option.textContent =
                `[${storage.organizationName}] ${storage.name}`;

            selectElement.appendChild(option);
        });
    } catch (error) {
        console.error("저장소 목록 로딩 실패:", error);
        showFeedback("danger", error.message);
    }
}

async function loadExpiringInventories(page) {
    clearSelection();

    const storageId =
        document.getElementById("search-storage").value;

    const filterType =
        document.getElementById("search-filter").value;

    const sortDirection =
        document.getElementById("search-sort").value;

    let url = `/api/core/expiring?page=${page}&size=20`;

    if (storageId) {
        url += `&storageId=${storageId}`;
    }

    if (filterType) {
        url += `&filterType=${filterType}`;
    }

    if (sortDirection) {
        url += `&sortDirection=${sortDirection}`;
    }

    try {
        const response = await apiFetch(url, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error(
                await getErrorMessage(
                    response,
                    "재고를 불러오지 못했습니다."
                )
            );
        }

        const result = await response.json();
        const pageResponse = result.data || result;

        visibleInventories = pageResponse.content || [];
        loadedPage = pageResponse.page ?? page;

        renderTable(
            visibleInventories,
            loadedPage,
            pageResponse.size ?? 20
        );

        renderPagination(pageResponse);
    } catch (error) {
        console.error("데이터 로딩 실패:", error);

        visibleInventories = [];

        document.getElementById(
            "inventory-table-body"
        ).innerHTML = `
            <tr>
                <td colspan="8"
                    class="text-center text-danger py-5">
                    ${escapeHtml(error.message)}
                </td>
            </tr>
        `;

        updateSelectionSummary();
    }
}

function renderTable(items, currentPage, size) {
    const tbody =
        document.getElementById("inventory-table-body");

    tbody.innerHTML = "";

    if (items.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8"
                    class="text-center text-secondary py-5">
                    조건에 일치하는 재고가 없습니다.
                </td>
            </tr>
        `;

        updateSelectionSummary();
        return;
    }

    items.forEach((item, index) => {
        const rowNumber =
            (currentPage * size) + (index + 1);

        const expired = isExpired(item.expirationDate);

        const detailUrl =
            `/storages/${item.storageId}` +
            `/pack-units/${item.medicinePackageUnitId}`;

        const row = document.createElement("tr");

        row.style.cursor = "pointer";
        row.tabIndex = 0;
        row.setAttribute("role", "link");
        row.setAttribute(
            "aria-label",
            `${item.medicineName} 재고 상세 보기`
        );

        row.innerHTML = `
            <td class="text-center">
                ${expired ? `
                    <input type="checkbox"
                           class="form-check-input inventory-select"
                           data-inventory-id="${item.inventoryId}"
                           aria-label="${escapeHtml(item.medicineName)} 선택">
                ` : ""}
            </td>
            <td class="text-center text-secondary">
                ${rowNumber}
            </td>
            <td>
                ${escapeHtml(item.organizationName || "-")}
            </td>
            <td>
                ${escapeHtml(item.storageName || "-")}
                /
                ${escapeHtml(item.zoneName || "-")}
            </td>
            <td style="white-space: normal; overflow-wrap: anywhere;">
                <strong class="d-block">
                    ${escapeHtml(item.medicineName)}
                </strong>
                <span class="d-block text-secondary small fw-normal mt-1">
                    ${escapeHtml(item.packUnitName || "-")}
                </span>
            </td>
            <td>
                ${escapeHtml(item.lotNumber || "-")}
            </td>
            <td class="text-center">
                ${formatNumber(item.currentQuantity)}개
            </td>
            <td class="text-center
                       ${expired ? "text-danger fw-bold" : ""}">
                ${escapeHtml(getExpirationLabel(item.expirationDate))}
            </td>
        `;

        const checkbox = row.querySelector(".inventory-select");

        if (checkbox) {
            checkbox.addEventListener("click", event => {
                event.stopPropagation();
            });

            checkbox.addEventListener("keydown", event => {
                event.stopPropagation();
            });

            checkbox.addEventListener("change", () => {
                updateInventorySelection(item, checkbox.checked);
            });
        }

        row.addEventListener("click", () => {
            window.location.href = detailUrl;
        });

        row.addEventListener("keydown", event => {
            if (event.target !== row) {
                return;
            }

            if (event.key !== "Enter" && event.key !== " ") {
                return;
            }

            event.preventDefault();
            window.location.href = detailUrl;
        });

        tbody.appendChild(row);
    });

    updateSelectionSummary();
}

function updateInventorySelection(item, selected) {
    if (!isExpired(item.expirationDate)) {
        return;
    }

    if (selected) {
        selectedInventories.set(
            Number(item.inventoryId),
            item
        );
    } else {
        selectedInventories.delete(
            Number(item.inventoryId)
        );
    }

    updateSelectionSummary();
}

function selectVisibleExpiredInventories(selected) {
    const expiredInventories =
        visibleInventories.filter(item =>
            isExpired(item.expirationDate)
        );

    expiredInventories.forEach(item => {
        const inventoryId = Number(item.inventoryId);

        if (selected) {
            selectedInventories.set(inventoryId, item);
        } else {
            selectedInventories.delete(inventoryId);
        }
    });

    document.querySelectorAll(
        ".inventory-select:not(:disabled)"
    ).forEach(checkbox => {
        checkbox.checked = selected;
    });

    updateSelectionSummary();
}

function updateSelectionSummary() {
    const selectedItems =
        Array.from(selectedInventories.values());

    const totalQuantity =
        selectedItems.reduce(
            (total, item) =>
                total + Number(item.currentQuantity || 0),
            0
        );

    document.getElementById(
        "selected-count"
    ).textContent = `${selectedItems.length}건 선택`;

    document.getElementById(
        "selected-total-quantity"
    ).textContent = formatNumber(totalQuantity);

    document.getElementById(
        "dispose-selected-button"
    ).disabled = selectedItems.length === 0;

    const selectableItems =
        visibleInventories.filter(item =>
            isExpired(item.expirationDate)
        );

    const selectAllCheckbox =
        document.getElementById("select-all-expired");

    const selectedVisibleCount =
        selectableItems.filter(item =>
            selectedInventories.has(
                Number(item.inventoryId)
            )
        ).length;

    selectAllCheckbox.disabled =
        selectableItems.length === 0;

    selectAllCheckbox.checked =
        selectableItems.length > 0
        && selectedVisibleCount === selectableItems.length;

    selectAllCheckbox.indeterminate =
        selectedVisibleCount > 0
        && selectedVisibleCount < selectableItems.length;
}

function clearSelection() {
    selectedInventories.clear();

    const selectAllCheckbox =
        document.getElementById("select-all-expired");

    if (selectAllCheckbox) {
        selectAllCheckbox.checked = false;
        selectAllCheckbox.indeterminate = false;
    }

    updateSelectionSummary();
}

function openSelectedDisposalModal() {
    const selectedItems =
        Array.from(selectedInventories.values());

    if (selectedItems.length === 0) {
        return;
    }

    const totalQuantity =
        selectedItems.reduce(
            (total, item) =>
                total + Number(item.currentQuantity || 0),
            0
        );

    document.getElementById(
        "confirm-selected-count"
    ).textContent = `${selectedItems.length}건`;

    document.getElementById(
        "confirm-selected-total-quantity"
    ).textContent = `${formatNumber(totalQuantity)}개`;

    const itemContainer =
        document.getElementById(
            "selected-disposal-items"
        );

    itemContainer.innerHTML = "";

    selectedItems.forEach(item => {
        const itemElement =
            document.createElement("div");

        itemElement.className =
            "py-2 border-bottom";

        itemElement.textContent =
            `${item.medicineName} · ` +
            `LOT ${item.lotNumber} · ` +
            `${formatNumber(item.currentQuantity)}개`;

        itemContainer.appendChild(itemElement);
    });

    const modalError =
        document.getElementById(
            "selected-disposal-modal-error"
        );

    modalError.hidden = true;
    modalError.textContent = "";

    bootstrap.Modal
        .getOrCreateInstance(
            document.getElementById(
                "selected-disposal-confirm-modal"
            )
        )
        .show();
}

async function disposeSelectedInventories() {
    const inventoryIds =
        Array.from(selectedInventories.keys());

    if (inventoryIds.length === 0) {
        return;
    }

    const confirmButton =
        document.getElementById(
            "confirm-selected-disposal-button"
        );

    const modalError =
        document.getElementById(
            "selected-disposal-modal-error"
        );

    confirmButton.disabled = true;
    confirmButton.textContent = "처리 중...";
    modalError.hidden = true;

    try {
        const response = await apiFetch(
            "/api/core/inventories/expired-disposals",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    inventoryIds
                })
            }
        );

        if (!response.ok) {
            throw new Error(
                await getErrorMessage(
                    response,
                    "선택 재고 폐기에 실패했습니다."
                )
            );
        }

        bootstrap.Modal
            .getOrCreateInstance(
                document.getElementById(
                    "selected-disposal-confirm-modal"
                )
            )
            .hide();

        showFeedback(
            "success",
            `${inventoryIds.length}건의 재고를 폐기했습니다.`
        );

        await loadExpiringInventories(loadedPage);
    } catch (error) {
        console.error("선택 재고 폐기 실패:", error);

        modalError.textContent = error.message;
        modalError.hidden = false;
    } finally {
        confirmButton.disabled = false;
        confirmButton.textContent =
            "선택 재고 폐기하기";
    }
}

function renderPagination(pageData) {
    const paginationContainer =
        document.getElementById("pagination");

    paginationContainer.innerHTML = "";

    const currentPage = pageData.page ?? 0;
    const totalPages = pageData.totalPages;

    if (totalPages <= 1) {
        return;
    }

    const prevItem = document.createElement("li");
    prevItem.className = "page-item";

    const prevButton = document.createElement("button");
    prevButton.className = "page-link";
    prevButton.innerText = "이전";
    prevButton.disabled = currentPage === 0;
    prevButton.onclick = () =>
        loadExpiringInventories(currentPage - 1);

    prevItem.appendChild(prevButton);
    paginationContainer.appendChild(prevItem);

    for (let page = 0; page < totalPages; page++) {
        const pageItem = document.createElement("li");
        pageItem.className = "page-item";

        if (page === currentPage) {
            pageItem.classList.add("active");
        }

        const pageButton =
            document.createElement("button");

        pageButton.className = "page-link";
        pageButton.innerText = page + 1;
        pageButton.onclick = () =>
            loadExpiringInventories(page);

        pageItem.appendChild(pageButton);
        paginationContainer.appendChild(pageItem);
    }

    const nextItem = document.createElement("li");
    nextItem.className = "page-item";

    const nextButton = document.createElement("button");
    nextButton.className = "page-link";
    nextButton.innerText = "다음";
    nextButton.disabled =
        currentPage >= totalPages - 1;

    nextButton.onclick = () =>
        loadExpiringInventories(currentPage + 1);

    nextItem.appendChild(nextButton);
    paginationContainer.appendChild(nextItem);
}

function resetSearch() {
    document.getElementById(
        "search-storage"
    ).value = "";

    document.getElementById(
        "search-filter"
    ).value = "ALL";

    document.getElementById(
        "search-sort"
    ).value = "ASC";

    loadExpiringInventories(0);
}

function isExpired(expirationDate) {
    return expirationDate < getTodayText();
}

function getTodayText() {
    const today = new Date();

    const year = today.getFullYear();
    const month =
        String(today.getMonth() + 1).padStart(2, "0");
    const day =
        String(today.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}
function getExpirationLabel(expirationDate) {
    if (!expirationDate) {
        return "-";
    }

    const expiration = Date.parse(`${expirationDate}T00:00:00Z`);
    const today = Date.parse(`${getTodayText()}T00:00:00Z`);

    if (!Number.isFinite(expiration)) {
        return "-";
    }

    const daysLeft = Math.round(
        (expiration - today) / (24 * 60 * 60 * 1000)
    );

    if (daysLeft < 0) {
        return "만료";
    }

    if (daysLeft === 0) {
        return "오늘까지";
    }

    return `${daysLeft}일 전`;
}

function formatNumber(value) {
    return Number(value || 0).toLocaleString();
}

function showFeedback(type, message) {
    const feedback =
        document.getElementById("disposal-feedback");

    feedback.className = `alert alert-${type}`;
    feedback.textContent = message;
    feedback.hidden = false;
}

async function getErrorMessage(response, fallbackMessage) {
    try {
        const body = await response.json();

        return body.error?.message
            ?? body.message
            ?? fallbackMessage;
    } catch (error) {
        return fallbackMessage;
    }
}

function escapeHtml(value) {
    const element = document.createElement("div");

    element.textContent =
        value == null ? "" : String(value);

    return element.innerHTML;
}