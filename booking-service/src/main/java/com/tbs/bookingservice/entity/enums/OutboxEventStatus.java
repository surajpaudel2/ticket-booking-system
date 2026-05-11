package com.tbs.bookingservice.entity.enums;

public enum OutboxEventStatus {

    // Event saved to outbox table, not yet picked up by the scheduler.
    PENDING,

    // Event successfully published to RabbitMQ by the outbox scheduler.
    PUBLISHED,

    // Event failed to publish after retryCount > 3 — requires manual investigation.
    FAILED
}
