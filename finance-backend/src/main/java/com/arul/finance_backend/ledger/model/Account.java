package com.arul.finance_backend.ledger.model;

import java.time.Instant;

import com.arul.finance_backend.ledger.enums.AccountStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A money-holding account.
 *
 * Balance is stored in MINOR UNITS (e.g. cents/paise) as a long.
 * NEVER use double/float for money — floating point cannot represent
 * 0.10 exactly and silently loses fractions of a cent at scale.
 */
@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owner of the account (FK to your existing User.userId). */
    @Column(nullable = false)
    private Long ownerUserId;

    /** ISO-4217 currency code, e.g. "USD", "INR". */
    @Column(nullable = false, length = 3)
    private String currency;

    /** Current balance in minor units (cents). */
    @Column(nullable = false)
    private long balanceMinor;

    /**
     * Optimistic-lock guard. Hibernate increments this on every update and
     * fails the commit if another transaction changed the row in between.
     * This is the second line of defence against double-spend, on top of
     * the pessimistic row lock taken during a transfer.
     */
    @Version
    private long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
