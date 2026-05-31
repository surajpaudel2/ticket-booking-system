package com.tbs.bookingservice.mapper;

import com.tbs.bookingservice.entity.Booking;
import com.tbs.bookingservice.entity.BookingAttempt;
import com.tbs.bookingservice.messaging.payload.outbound.BookingAttemptNudgeEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.BookingExpiredEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.BookingReminderEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.SeatsReleaseEventPayload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SchedulerMapper {

    public BookingReminderEventPayload toReminderPayload(Booking booking, int minutesRemaining) {
        return new BookingReminderEventPayload(
                booking.getId(), booking.getUserId(), null, null,
                booking.getFixtureId(), null, null, null,
                booking.getPaymentIntentId(), booking.getExpiresAt(), minutesRemaining);
    }

    public BookingExpiredEventPayload toExpiredPayload(Booking booking) {
        return new BookingExpiredEventPayload(
                booking.getId(), booking.getBookingAttemptId(), booking.getUserId(),
                null, null,
                booking.getFixtureId(), null, null, null,
                booking.getRequestedSeats(), LocalDateTime.now());
    }

    public SeatsReleaseEventPayload toSeatsReleasePayload(Booking booking, String reason) {
        return new SeatsReleaseEventPayload(
                booking.getFixtureId(),
                booking.getRequestedSeats(),
                booking.getId(),
                reason);
    }

    public BookingAttemptNudgeEventPayload toNudgePayload(BookingAttempt attempt) {
        return new BookingAttemptNudgeEventPayload(
                attempt.getId(), attempt.getUserId(), attempt.getFixtureId(),
                attempt.getSeatHint(), attempt.getCreatedAt());
    }
}