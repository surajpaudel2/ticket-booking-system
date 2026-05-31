package com.tbs.notificationservice.mapper;

import com.tbs.notificationservice.client.dto.response.FixtureResponse;
import com.tbs.notificationservice.client.dto.response.UserResponse;
import com.tbs.notificationservice.messaging.payload.inbound.BookingAttemptNudgeEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingConfirmedPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingExpiredEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingFailedEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingPaymentInitiatedEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingReminderEventPayload;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Component
public class EmailContextMapper {

    public Context toBookingExpiredContext(BookingExpiredEventPayload p) {
        Context ctx = new Context();
        ctx.setVariable("recipientName",  p.recipientName());
        ctx.setVariable("homeTeamName",   p.homeTeamName());
        ctx.setVariable("awayTeamName",   p.awayTeamName());
        ctx.setVariable("matchDateTime",  p.currentScheduledStartTime());
        ctx.setVariable("requestedSeats", p.requestedSeats());
        ctx.setVariable("expiredAt",      p.expiredAt());
        return ctx;
    }

    public Context toNudgeContext(BookingAttemptNudgeEventPayload p,
                                  UserResponse user, FixtureResponse fixture) {
        Context ctx = new Context();
        ctx.setVariable("firstName",      user.firstName());
        ctx.setVariable("homeTeamName",   fixture.homeTeamName());
        ctx.setVariable("awayTeamName",   fixture.awayTeamName());
        ctx.setVariable("matchDateTime",  fixture.fixtureDateTime());
        ctx.setVariable("availableSeats", fixture.availableSeats());
        ctx.setVariable("seatHint",       p.seatHint());
        ctx.setVariable("attemptedAt",    p.attemptedAt());
        return ctx;
    }

    public Context toReminderContext(BookingReminderEventPayload p) {
        Context ctx = new Context();
        ctx.setVariable("recipientName",    p.recipientName());
        ctx.setVariable("homeTeamName",     p.homeTeamName());
        ctx.setVariable("awayTeamName",     p.awayTeamName());
        ctx.setVariable("matchDateTime",    p.currentScheduledStartTime());
        ctx.setVariable("expiresAt",        p.expiresAt());
        ctx.setVariable("minutesRemaining", p.minutesRemaining());
        return ctx;
    }

    public Context toBookingFailedContext(BookingFailedEventPayload p) {
        Context ctx = new Context();
        ctx.setVariable("recipientName",  p.recipientName());
        ctx.setVariable("homeTeamName",   p.homeTeamName());
        ctx.setVariable("awayTeamName",   p.awayTeamName());
        ctx.setVariable("matchDateTime",  p.currentScheduledStartTime());
        ctx.setVariable("failureReason",  p.failureReason());
        ctx.setVariable("requestedSeats", p.requestedSeats());
        ctx.setVariable("availableSeats", p.availableSeats());
        return ctx;
    }

    public Context toPaymentInitiatedContext(BookingPaymentInitiatedEventPayload p) {
        Context ctx = new Context();
        ctx.setVariable("recipientName",   p.recipientName());
        ctx.setVariable("homeTeamName",    p.homeTeamName());
        ctx.setVariable("awayTeamName",    p.awayTeamName());
        ctx.setVariable("matchDateTime",   p.currentScheduledStartTime());
        ctx.setVariable("expiresAt",       p.expiresAt());
        ctx.setVariable("requestedSeats",  p.requestedSeats());
        ctx.setVariable("totalAmount",     p.totalAmount());
        ctx.setVariable("paymentIntentId", p.paymentIntentId());
        return ctx;
    }

    public Context toBookingConfirmedContext(BookingConfirmedPayload p) {
        Context ctx = new Context();
        ctx.setVariable("recipientFullName", p.recipientFullName());
        ctx.setVariable("homeTeamName",      p.homeTeamName());
        ctx.setVariable("awayTeamName",      p.awayTeamName());
        ctx.setVariable("stadiumName",       p.stadiumName());
        ctx.setVariable("matchDateTime",     p.fixtureDateTime());
        ctx.setVariable("numberOfTickets",   p.numberOfTickets());
        ctx.setVariable("totalAmountPaid",   p.totalAmountPaid());
        ctx.setVariable("confirmedAt",       p.confirmedAt());
        return ctx;
    }
}