package com.arul.finance_backend.ledger.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * @param amountMinor amount in minor units (cents). Integer, never a decimal.
 */
public record TransferRequest(
        @NotNull Long sourceAccountId,
        @NotNull Long destAccountId,
        @Positive(message = "amountMinor must be > 0") long amountMinor,
        @NotNull @Size(min = 3, max = 3) String currency
) {}
