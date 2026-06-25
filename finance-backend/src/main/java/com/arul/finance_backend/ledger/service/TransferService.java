package com.arul.finance_backend.ledger.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arul.finance_backend.ledger.dto.TransferRequest;
import com.arul.finance_backend.ledger.enums.AccountStatus;
import com.arul.finance_backend.ledger.enums.Direction;
import com.arul.finance_backend.ledger.enums.TransferStatus;
import com.arul.finance_backend.ledger.exception.AccountNotFoundException;
import com.arul.finance_backend.ledger.exception.CurrencyMismatchException;
import com.arul.finance_backend.ledger.exception.InsufficientFundsException;
import com.arul.finance_backend.ledger.exception.InvalidTransferException;
import com.arul.finance_backend.ledger.model.Account;
import com.arul.finance_backend.ledger.model.LedgerEntry;
import com.arul.finance_backend.ledger.model.Transfer;
import com.arul.finance_backend.ledger.outbox.OutboxEvent;
import com.arul.finance_backend.ledger.outbox.OutboxEventRepository;
import com.arul.finance_backend.ledger.repository.AccountRepository;
import com.arul.finance_backend.ledger.repository.LedgerEntryRepository;
import com.arul.finance_backend.ledger.repository.TransferRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * Move money from one account to another, exactly once.
     *
     * Guarantees:
     *  - IDEMPOTENT: same idempotency key never moves money twice.
     *  - NO DOUBLE-SPEND: accounts are row-locked, so concurrent transfers
     *    on the same account are serialised; a balance can never go negative.
     *  - ATOMIC: the debit, the credit, the two ledger rows, the transfer
     *    record and the outbox event all commit together or not at all.
     *  - AUDITABLE: every movement leaves a balanced pair of ledger entries.
     */
    @Transactional
    public Transfer transfer(String idempotencyKey, TransferRequest req) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidTransferException("Idempotency-Key header is required");
        }

        // 1. Idempotency fast path: already processed -> return the prior result.
        var existing = transferRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("transfer_idempotent_hit key={} transferId={}",
                    idempotencyKey, existing.get().getId());
            return existing.get();
        }

        // 2. Validate the request shape.
        if (req.sourceAccountId().equals(req.destAccountId())) {
            throw new InvalidTransferException("Source and destination must differ");
        }
        if (req.amountMinor() <= 0) {
            throw new InvalidTransferException("Amount must be positive");
        }

        // 3. Lock both accounts in a DETERMINISTIC order (lowest id first).
        //    Consistent ordering is what prevents deadlocks when A->B and
        //    B->A transfers run at the same time.
        Long firstId  = Math.min(req.sourceAccountId(), req.destAccountId());
        Long secondId = Math.max(req.sourceAccountId(), req.destAccountId());
        Account first  = lock(firstId);
        Account second = lock(secondId);

        Account source = source(first, second, req.sourceAccountId());
        Account dest   = (source == first) ? second : first;

        // 4. Business invariants.
        if (source.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransferException("Source account is not active");
        }
        if (dest.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransferException("Destination account is not active");
        }
        if (!source.getCurrency().equals(req.currency())
                || !dest.getCurrency().equals(req.currency())) {
            throw new CurrencyMismatchException(
                    "Currency mismatch between request and accounts");
        }
        if (source.getBalanceMinor() < req.amountMinor()) {
            // Rolls back the whole transaction; no idempotency row is written,
            // so a later retry with more funds will succeed.
            throw new InsufficientFundsException(
                    "Insufficient funds in account " + source.getId());
        }

        // 5. Apply the movement (integer arithmetic on minor units).
        Instant now = Instant.now();
        source.setBalanceMinor(source.getBalanceMinor() - req.amountMinor());
        dest.setBalanceMinor(dest.getBalanceMinor() + req.amountMinor());
        source.setUpdatedAt(now);
        dest.setUpdatedAt(now);

        // 6. Record the transfer. The UNIQUE idempotency_key is the hard
        //    backstop: if two identical requests somehow race past step 1,
        //    the second insert fails and no second movement is committed.
        UUID transferId = UUID.randomUUID();
        Transfer transfer = new Transfer(
                transferId, idempotencyKey, source.getId(), dest.getId(),
                req.amountMinor(), req.currency(), TransferStatus.COMPLETED, null, now);
        transferRepository.save(transfer);

        // 7. Double-entry: one DEBIT, one CREDIT, summing to zero.
        ledgerEntryRepository.save(new LedgerEntry(
                null, transferId, source.getId(), Direction.DEBIT,
                req.amountMinor(), req.currency(), now));
        ledgerEntryRepository.save(new LedgerEntry(
                null, transferId, dest.getId(), Direction.CREDIT,
                req.amountMinor(), req.currency(), now));

        // 8. Outbox event, committed in the same transaction.
        outboxRepository.save(new OutboxEvent(
                null, "Transfer", transferId.toString(), "TransferCompleted",
                toJson(transfer), false, now, null));

        log.info("transfer_completed transferId={} key={} from={} to={} amountMinor={} {}",
                transferId, idempotencyKey, source.getId(), dest.getId(),
                req.amountMinor(), req.currency());

        return transfer;
    }

    public Transfer getByIdempotencyKey(String key) {
        return transferRepository.findByIdempotencyKey(key)
                .orElseThrow(() -> new InvalidTransferException(
                        "No transfer for idempotency key " + key));
    }

    private Account lock(Long id) {
        return accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));
    }

    private Account source(Account first, Account second, Long sourceId) {
        return first.getId().equals(sourceId) ? first : second;
    }

    private String toJson(Transfer t) {
        try {
            return objectMapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            // Should never happen for a flat record; fail loudly if it does.
            throw new IllegalStateException("Failed to serialise transfer event", e);
        }
    }
}
