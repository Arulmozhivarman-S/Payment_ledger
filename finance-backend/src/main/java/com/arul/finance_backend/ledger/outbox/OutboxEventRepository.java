package com.arul.finance_backend.ledger.outbox;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /** Batch picked up by the relay each poll. */
    List<OutboxEvent> findTop100ByPublishedFalseOrderByCreatedAtAsc();
}
