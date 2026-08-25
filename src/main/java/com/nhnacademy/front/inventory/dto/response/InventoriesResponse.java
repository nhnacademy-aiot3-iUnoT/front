package com.nhnacademy.front.inventory.dto.response;

import java.time.LocalDate;

public record InventoriesResponse(

        Long storageId,
        Long packUnitId,
        String productName,
        String itemCode,
        String packUnit,
        LocalDate expirationDate,
        String storageName,
        Integer totalQuantity



) {
}
