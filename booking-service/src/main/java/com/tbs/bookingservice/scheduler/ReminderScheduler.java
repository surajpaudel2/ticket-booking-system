package com.tbs.bookingservice.scheduler;

import com.tbs.bookingservice.entity.Booking;
import com.tbs.bookingservice.entity.enums.BookingStatus;
import com.tbs.bookingservice.entity.enums.OutboxEventType;
import com.tbs.bookingservice.messaging.payload.outbound.BookingReminderEventPayload;
import com.tbs.bookingservice.repository.BookingRepository;
import com.tbs.bookingservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scans PENDING bookings and saves reminder outbox events at the 5-min and 10-min marks.
 * Guards: booking must be PENDING, reminder must not already be sent, booking must not be expired.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ReminderScheduler {

    private final BookingRepository bookingRepository;
    private final OutboxService outboxService;

    // Finds bookings expiring within 10 min that have not received reminder 1 yet
    @Scheduled(fixedDelay = 60000)
    public void processReminder1() {
        LocalDateTime tenMinWindow = LocalDateTime.now().plusMinutes(10);
        List<Booking> bookings = bookingRepository
                .findAllByStatusAndExpiresAtAfterAndReminder1SentAtIsNull(BookingStatus.PENDING, LocalDateTime.now());
        bookings.stream()
                .filter(b -> b.getExpiresAt().isBefore(tenMinWindow))
                .forEach(booking -> {
                    saveReminder1OutboxEvent(booking);
                    booking.setReminder1SentAt(LocalDateTime.now());
                    bookingRepository.save(booking);
                    log.info("Reminder1 outbox event saved for bookingId={}", booking.getId());
                });
    }

    // Finds bookings expiring within 5 min that have not received reminder 2 yet
    @Scheduled(fixedDelay = 60000)
    public void processReminder2() {
        LocalDateTime fiveMinWindow = LocalDateTime.now().plusMinutes(5);
        List<Booking> bookings = bookingRepository
                .findAllByStatusAndExpiresAtAfterAndReminder2SentAtIsNull(BookingStatus.PENDING, LocalDateTime.now());
        bookings.stream()
                .filter(b -> b.getExpiresAt().isBefore(fiveMinWindow))
                .forEach(booking -> {
                    saveReminder2OutboxEvent(booking);
                    booking.setReminder2SentAt(LocalDateTime.now());
                    bookingRepository.save(booking);
                    log.info("Reminder2 outbox event saved for bookingId={}", booking.getId());
                });
    }

    // Builds and saves the reminder 1 outbox event with 10 minutes remaining context
    private void saveReminder1OutboxEvent(Booking booking) {
        BookingReminderEventPayload payload = buildReminderPayload(booking, 10);
        outboxService.saveEvent(OutboxEventType.BOOKING_REMINDER_1, payload,
                booking.getId(), booking.getBookingAttemptId());
    }

    // Builds and saves the reminder 2 outbox event with 5 minutes remaining context
    private void saveReminder2OutboxEvent(Booking booking) {
        BookingReminderEventPayload payload = buildReminderPayload(booking, 5);
        outboxService.saveEvent(OutboxEventType.BOOKING_REMINDER_2, payload,
                booking.getId(), booking.getBookingAttemptId());
    }

    // Constructs the reminder payload — fixture team names fetched by notification-service using fixtureId
    private BookingReminderEventPayload buildReminderPayload(Booking booking, int minutesRemaining) {
        return new BookingReminderEventPayload(
                booking.getId(), booking.getUserId(), null, null,
                booking.getFixtureId(), null, null, null,
                booking.getPaymentIntentId(), booking.getExpiresAt(), minutesRemaining);
    }
}
