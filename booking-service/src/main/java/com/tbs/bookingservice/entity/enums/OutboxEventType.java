package com.tbs.bookingservice.entity.enums;

public enum OutboxEventType {

    // Published to seats.release binding — triggers Event Service to restore seat count in DB and Redis.
    // Triggered by: payment failure compensation, booking expiry compensation.
    SEATS_RELEASE,

    // Published to booking.failed binding — triggers Notification Service to send failure email.
    // Triggered by: EVENT_NOT_FOUND, INSUFFICIENT_SEATS, PAYMENT_FAILED failure paths.
    BOOKING_FAILED,

    // Published to booking.reminder.1 binding — triggers first reminder email at ~5 min mark.
    // Created by reminder scheduler when PENDING booking has expiresAt <= now + 10 min and reminder1SentAt is null.
    BOOKING_REMINDER_1,

    // Published to booking.reminder.2 binding — triggers second reminder email at ~10 min mark.
    // Created by reminder scheduler when PENDING booking has expiresAt <= now + 5 min and reminder2SentAt is null.
    BOOKING_REMINDER_2,

    // Published to booking.expired binding — triggers expiry email to customer.
    // Created by expiry scheduler when PENDING booking expiresAt < now.
    BOOKING_EXPIRED,

    // Published to booking.attempted.nudge binding — triggers abandoned booking nudge email.
    // Created by nudge scheduler when ATTEMPTED BookingAttempt is older than 30 min,
    // nudgeSentAt is null, and seatHint is not null.
    BOOKING_ATTEMPTED_NUDGE
}
