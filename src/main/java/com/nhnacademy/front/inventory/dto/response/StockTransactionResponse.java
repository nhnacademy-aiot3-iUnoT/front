package com.nhnacademy.front.inventory.dto.response;

import com.nhnacademy.front.inventory.dto.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockTransactionResponse(
        Long stockTransactionId,
        String medicineName,
        String packUnit,
        TransactionType transactionType,
        Integer quantity,
        String reason,
        String memo,

        UUID processedBy,

        String processedByName,
        LocalDateTime processedAt
) {
    public boolean isIncoming() {
        return transactionType == TransactionType.INBOUND
                || transactionType == TransactionType.TRANSFER_IN
                || transactionType == TransactionType.INFO_CORRECTION_IN;
    }
}
