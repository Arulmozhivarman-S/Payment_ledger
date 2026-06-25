package com.arul.finance_backend.ledger.model;

import java.time.Instant;
import java.util.UUID;

import com.arul.finance_backend.ledger.enums.Direction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One immutable leg of a double-entry posting.
 *
 * Every transfer writes exactly two rows that sum to zero:
 *   DEBIT  amount on the source account
 *   CREDIT amount on the destination account
 *
 * Ledger entries are append-only — they are never updated or deleted.
 * The current balance can always be reconstructed by summing entries,
 * which makes the system auditable.
 */
@Entity
@Table(name = "ledger_entry",
       indexes = {
           @Index(name = "idx_ledger_transfer", columnList = "transferId"),
           @Index(name = "idx_ledger_account",  columnList = "accountId")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Groups the two legs of the same transfer. */
    @Column(nullable = false)
    private UUID transferId;

    @Column(nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Column(nullable = false)
    private long amountMinor;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private Instant createdAt;
}
