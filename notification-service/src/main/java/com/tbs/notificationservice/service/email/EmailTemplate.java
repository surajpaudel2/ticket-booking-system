package com.tbs.notificationservice.service.email;

public enum EmailTemplate {

    BOOKING_EXPIRED   ("email/booking-expired",  "email.subject.booking.expired"),
    BOOKING_NUDGE     ("email/booking-nudge",     "email.subject.booking.nudge"),
    BOOKING_REMINDER_1("email/booking-reminder",  "email.subject.booking.reminder1"),
    BOOKING_REMINDER_2("email/booking-reminder",  "email.subject.booking.reminder2"),
    BOOKING_FAILED    ("email/booking-failed",    "email.subject.booking.failed"),
    PAYMENT_INITIATED ("email/payment-initiated", "email.subject.payment.initiated"),
    BOOKING_CONFIRMED ("email/booking-confirmed", "email.subject.booking.confirmed");

    public final String templateName;
    public final String subjectKey;

    EmailTemplate(String templateName, String subjectKey) {
        this.templateName = templateName;
        this.subjectKey   = subjectKey;
    }
}