package com.tbs.bookingservice.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbs.bookingservice.entity.OutboxEvent;
import com.tbs.bookingservice.entity.enums.OutboxEventStatus;
import com.tbs.bookingservice.messaging.payload.outbound.BookingAttemptNudgeEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.BookingExpiredEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.BookingFailedEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.BookingReminderEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.SeatsReleaseEventPayload;
import com.tbs.bookingservice.messaging.publisher.BookingEventPublisher;
import com.tbs.bookingservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scans the outbox_event table for PENDING events and publishes them to RabbitMQ.
 * Implements at-least-once delivery guarantee. Events with retryCount >= 3 are marked FAILED.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxScheduler {

    private static final int MAX_RETRY = 3;

    private final OutboxEventRepository outboxEventRepository;
    private final BookingEventPublisher bookingEventPublisher;
    private final ObjectMapper objectMapper;

    // Fetches all publishable PENDING events and attempts delivery every 5 seconds
    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findAllByStatusAndRetryCountLessThan(
                OutboxEventStatus.PENDING, MAX_RETRY);
        pending.forEach(this::publishOutboxEvent);
    }

    // Deserializes, routes to publisher, then marks PUBLISHED or increments retry on failure
    private void publishOutboxEvent(OutboxEvent event) {
        try {
            Object payload = deserializePayload(event);
            routeToPublisher(event, payload);
            event.setStatus(OutboxEventStatus.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now());
        } catch (Exception ex) {
            event.setRetryCount(event.getRetryCount() + 1);
            if (event.getRetryCount() >= MAX_RETRY) {
                event.setStatus(OutboxEventStatus.FAILED);
                log.error("Outbox event permanently failed id={} type={}: {}",
                        event.getId(), event.getEventType(), ex.getMessage());
            }
        } finally {
            try {
                outboxEventRepository.save(event); // single save point
            } catch (Exception saveEx) {
                log.error("Failed to persist outbox state for id={}: {}",
                        event.getId(), saveEx.getMessage());
            }
        }
    }

    // Deserializes JSON payload to the correct record type based on eventType
    private Object deserializePayload(OutboxEvent event) throws Exception {
        return switch (event.getEventType()) {
            case SEATS_RELEASE -> objectMapper.readValue(event.getPayload(), SeatsReleaseEventPayload.class);
            case BOOKING_FAILED -> objectMapper.readValue(event.getPayload(), BookingFailedEventPayload.class);
            case BOOKING_REMINDER_1, BOOKING_REMINDER_2 ->
                    objectMapper.readValue(event.getPayload(), BookingReminderEventPayload.class);
            case BOOKING_EXPIRED -> objectMapper.readValue(event.getPayload(), BookingExpiredEventPayload.class);
            case BOOKING_ATTEMPTED_NUDGE ->
                    objectMapper.readValue(event.getPayload(), BookingAttemptNudgeEventPayload.class);
        };
    }

    // Routes the deserialized payload to the correct publisher method
    private void routeToPublisher(OutboxEvent event, Object payload) {
        switch (event.getEventType()) {
            case SEATS_RELEASE -> bookingEventPublisher.publishSeatsRelease((SeatsReleaseEventPayload) payload);
            case BOOKING_FAILED -> bookingEventPublisher.publishBookingFailed((BookingFailedEventPayload) payload);
            case BOOKING_REMINDER_1 -> bookingEventPublisher.publishReminder1((BookingReminderEventPayload) payload);
            case BOOKING_REMINDER_2 -> bookingEventPublisher.publishReminder2((BookingReminderEventPayload) payload);
            case BOOKING_EXPIRED -> bookingEventPublisher.publishBookingExpired((BookingExpiredEventPayload) payload);
            case BOOKING_ATTEMPTED_NUDGE ->
                    bookingEventPublisher.publishAttemptNudge((BookingAttemptNudgeEventPayload) payload);
        }
    }
}
