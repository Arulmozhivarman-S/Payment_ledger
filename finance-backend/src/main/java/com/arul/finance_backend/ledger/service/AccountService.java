package com.arul.finance_backend.ledger.service;

import java.time.Instant;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arul.finance_backend.ledger.enums.AccountStatus;
import com.arul.finance_backend.ledger.exception.AccountNotFoundException;
import com.arul.finance_backend.ledger.model.Account;
import com.arul.finance_backend.ledger.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    
    @Transactional
    public Account open(Long ownerUserId, String currency, long openingBalanceMinor) {

        Instant now = Instant.now();
        Account account = new Account(
                null, ownerUserId, currency.toUpperCase(),
                Math.max(0, openingBalanceMinor), 0L,
                AccountStatus.ACTIVE, now, now);
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Account get(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));
    }
}
