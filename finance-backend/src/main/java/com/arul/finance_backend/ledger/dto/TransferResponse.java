package com.arul.finance_backend.ledger.dto;

import java.time.Instant;
import java.util.UUID;

import com.arul.finance_backend.ledger.enums.TransferStatus;
import com.arul.finance_backend.ledger.model.Transfer;

public record TransferResponse(
        UUID transferId,
        String idempotencyKey,
        Long sourceAccountId,
        Long destAccountId,
        long amountMinor,
        String currency,
        TransferStatus status,
        Instant createdAt
) {
    public static TransferResponse from(Transfer t) {
        return new TransferResponse(
                t.getId(), t.getIdempotencyKey(), t.getSourceAccountId(),
                t.getDestAccountId(), t.getAmountMinor(), t.getCurrency(),
                t.getStatus(), t.getCreatedAt());
    }
}
