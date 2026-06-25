package com.arul.finance_backend.ledger.outbox;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transactional outbox row.
 *
 * The event is written in the SAME database transaction as the transfer,
 * so an event can never be lost (no "money moved but no event") and never
 * be emitted for a transfer that rolled back. A background relay then
 * publishes unpublished rows to the message broker at-least-once.
 */
@Entity
@Table(name = "outbox_event",
       indexes = @Index(name = "idx_outbox_unpublished", columnList = "published, createdAt"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String aggregateType;   // e.g. "Transfer"

    @Column(nullable = false)
    private String aggregateId;     // e.g. the transfer UUID

    @Column(nullable = false)
    private String eventType;       // e.g. "TransferCompleted"

    @Lob
    @Column(nullable = false)
    private String payload;         // JSON

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant publishedAt;
}
