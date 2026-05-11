package com.tbs.bookingservice.service;

import com.tbs.bookingservice.entity.enums.OutboxEventType;

/** Saves domain events to the outbox table for at-least-once delivery by OutboxScheduler. */
public interface OutboxService {

    /** Serializes payload to JSON and persists an OutboxEvent with status PENDING. */
    void saveEvent(OutboxEventType type, Object payload, Long bookingId, Long bookingAttemptId);
}
