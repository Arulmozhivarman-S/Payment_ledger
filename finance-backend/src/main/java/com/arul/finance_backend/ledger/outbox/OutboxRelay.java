package com.arul.finance_backend.ledger.outbox;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Polls the outbox and publishes unpublished events at-least-once, then
 * marks them published. Because the consumer side keys off the transfer
 * UUID, a duplicate delivery is harmless (idempotent consumer).
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private final OutboxEventRepository outboxRepository;
    private final DomainEventPublisher publisher;

    @Scheduled(fixedDelayString = "${ledger.outbox.poll-ms:2000}")
    @Transactional
    public void relay() {
        List<OutboxEvent> batch = outboxRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();
        if (batch.isEmpty()) return;

        for (OutboxEvent event : batch) {
            publisher.publish(event);
            event.setPublished(true);
            event.setPublishedAt(Instant.now());
        }
        outboxRepository.saveAll(batch);
        log.debug("outbox_relayed count={}", batch.size());
    }
}
