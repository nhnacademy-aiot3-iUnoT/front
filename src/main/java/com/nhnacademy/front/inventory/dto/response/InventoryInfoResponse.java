package com.nhnacademy.front.inventory.dto.response;


import com.nhnacademy.front.global.dto.PageResponse;


public record InventoryInfoResponse(

        Long medicinePackUnitId,
        Long storageId,
        String storageName,
        String itemCode,
        String productName,
        PageResponse<InventoryDetailResponse> inventories




) {
}
