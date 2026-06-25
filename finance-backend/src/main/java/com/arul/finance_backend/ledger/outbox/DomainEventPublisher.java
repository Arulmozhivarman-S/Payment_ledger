package com.arul.finance_backend.ledger.outbox;

/** Where outbox events go once committed. Swap the impl for Kafka in prod. */
public interface DomainEventPublisher {
    void publish(OutboxEvent event);
}
