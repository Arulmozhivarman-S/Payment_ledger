package com.arul.finance_backend.ledger.dto;

import com.arul.finance_backend.ledger.enums.AccountStatus;
import com.arul.finance_backend.ledger.model.Account;

public record AccountResponse(
        Long id,
        Long ownerUserId,
        String currency,
        long balanceMinor,
        AccountStatus status
) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getId(), a.getOwnerUserId(),
                a.getCurrency(), a.getBalanceMinor(), a.getStatus());
    }
}
