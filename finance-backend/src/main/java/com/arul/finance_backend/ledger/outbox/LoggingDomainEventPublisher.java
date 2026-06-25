package com.arul.finance_backend.ledger.outbox;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Default publisher: writes the event to the log. Lets the whole pipeline
 * run end-to-end with zero broker infrastructure. Replace with a Kafka
 * implementation (see KafkaDomainEventPublisher in CHANGES.md) by adding
 * the spring-kafka dependency and marking this bean @Primary off.
 */
@Component
@Slf4j
public class LoggingDomainEventPublisher implements DomainEventPublisher {

    @Override
    public void publish(OutboxEvent event) {
        log.info("event_published type={} aggregate={} id={} payload={}",
                event.getEventType(), event.getAggregateType(),
                event.getAggregateId(), event.getPayload());
    }
}
