package com.tbs.bookingservice.scheduler;

import com.tbs.bookingservice.entity.OutboxEvent;
import com.tbs.bookingservice.entity.enums.OutboxEventStatus;
import com.tbs.bookingservice.messaging.payload.outbound.*;
import com.tbs.bookingservice.messaging.publisher.BookingEventPublisher;
import com.tbs.bookingservice.repository.BookingOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxScheduler {

    private static final int MAX_RETRY = 3;

    private final BookingOutboxEventRepository outboxEventRepository;
    private final BookingEventPublisher  bookingEventPublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 30000)
    public void processOutboxEvents() {
        outboxEventRepository
                .findAllByStatusAndRetryCountLessThan(OutboxEventStatus.PENDING, MAX_RETRY)
                .forEach(this::publishEvent);
    }

    private void publishEvent(OutboxEvent event) {
        try {
            dispatch(event);
            markPublished(event);
        } catch (Exception ex) {
            log.error("Failed to publish outbox event id={} type={} attempt={}",
                    event.getId(), event.getEventType(), event.getRetryCount() + 1, ex);
            incrementRetry(event);
        }
    }

    private void dispatch(OutboxEvent event) throws Exception {
        switch (event.getEventType()) {
            case BOOKING_CONFIRMED -> bookingEventPublisher.publishBookingConfirmed(
                    deserialize(event.getPayload(), BookingConfirmedPayload.class));

            case BOOKING_EXPIRED -> bookingEventPublisher.publishBookingExpired(
                    deserialize(event.getPayload(), BookingExpiredEventPayload.class));

            case BOOKING_REMINDER_1 -> bookingEventPublisher.publishBookingReminder1(
                    deserialize(event.getPayload(), BookingReminderEventPayload.class));

            case BOOKING_REMINDER_2 -> bookingEventPublisher.publishBookingReminder2(
                    deserialize(event.getPayload(), BookingReminderEventPayload.class));

            case BOOKING_FAILED -> bookingEventPublisher.publishBookingFailed(
                    deserialize(event.getPayload(), BookingFailedEventPayload.class));

            // TODO : for this case, you have to give special treatment by checking the real data.
            case BOOKING_ATTEMPTED_NUDGE -> bookingEventPublisher.publishBookingAttemptNudge(
                    deserialize(event.getPayload(), BookingAttemptNudgeEventPayload.class));

            case SEATS_RELEASE -> bookingEventPublisher.publishSeatsRelease(
                    deserialize(event.getPayload(), SeatsReleaseEventPayload.class));
        }
    }

    private void markPublished(OutboxEvent event) {
        event.setStatus(OutboxEventStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
        outboxEventRepository.save(event);
        log.info("Published outbox event id={} type={}", event.getId(), event.getEventType());
    }

    private void incrementRetry(OutboxEvent event) {
        event.setRetryCount(event.getRetryCount() + 1);
        outboxEventRepository.save(event);
    }

    private <T> T deserialize(String json, Class<T> type) throws Exception {
        return objectMapper.readValue(json, type);
    }

}
