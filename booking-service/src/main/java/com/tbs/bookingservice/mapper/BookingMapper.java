package com.tbs.bookingservice.mapper;

import com.tbs.bookingservice.client.dto.request.InitiatePaymentRequest;
import com.tbs.bookingservice.client.dto.response.InitiatePaymentResponse;
import com.tbs.bookingservice.client.dto.request.ReserveSeatRequest;
import com.tbs.bookingservice.client.dto.response.ReserveSeatResponse;
import com.tbs.bookingservice.dto.cache.BookingPendingCache;
import com.tbs.bookingservice.dto.request.InitiateBookingRequest;
import com.tbs.bookingservice.dto.response.InitiateBookingResponse;
import com.tbs.bookingservice.entity.Booking;
import com.tbs.bookingservice.entity.BookingAttempt;
import com.tbs.bookingservice.entity.enums.BookingAttemptFailureReason;
import com.tbs.bookingservice.entity.enums.BookingAttemptStatus;
import com.tbs.bookingservice.entity.enums.BookingStatus;
import com.tbs.bookingservice.messaging.payload.outbound.BookingConfirmedPayload;
import com.tbs.bookingservice.messaging.payload.outbound.BookingFailedEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.BookingPaymentInitiatedEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.SeatsReleaseEventPayload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class BookingMapper {

    public ReserveSeatRequest toReserveSeatRequest(InitiateBookingRequest request) {
        return new ReserveSeatRequest(
                request.getFixtureId(),
                request.getRequestedSeats(),
                request.getUserId());
    }

    public InitiatePaymentRequest toInitiatePaymentRequest(Booking booking) {
        return new InitiatePaymentRequest(
                booking.getId(),
                booking.getTotalAmount(),
                "GBP",
                booking.getUserId());
    }

    public Booking toPendingBooking(Long bookingAttemptId, InitiateBookingRequest request, ReserveSeatResponse reserve) {
        double total = reserve.pricePerSeat() * request.getRequestedSeats();
        return Booking.builder()
                .userId(request.getUserId())
                .fixtureId(request.getFixtureId())
                .requestedSeats(request.getRequestedSeats())
                .pricePerSeat(reserve.pricePerSeat())
                .totalAmount(total)
                .recipientEmail(request.getRecipientEmail())
                .recipientFullName(request.getRecipientFullName())
                .status(BookingStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .bookingAttemptId(bookingAttemptId)
                .build();
    }

    public InitiateBookingResponse toInitiateBookingResponse(Booking booking,
                                                             InitiatePaymentResponse payment) {
        return new InitiateBookingResponse(
                booking.getId(),
                booking.getFixtureId(),
                payment.paymentIntentId(),
                payment.clientSecret(),
                booking.getTotalAmount(),
                booking.getRequestedSeats(),
                booking.getExpiresAt(),
                booking.getStatus().name());
    }

    public BookingFailedEventPayload toBookingFailedPayloadForAttempt(
            Long attemptId, Long userId, Long fixtureId,
            BookingAttemptFailureReason reason, ReserveSeatResponse reserve) {
        boolean isEventNotFound = reason == BookingAttemptFailureReason.EVENT_NOT_FOUND;
        return new BookingFailedEventPayload(
                attemptId, userId, null, null, fixtureId, 0,
                reason.name(), null,
                !isEventNotFound && reserve != null ? reserve.homeTeamName()              : null,
                !isEventNotFound && reserve != null ? reserve.awayTeamName()              : null,
                !isEventNotFound && reserve != null ? reserve.currentScheduledStartTime() : null);
    }

    public BookingFailedEventPayload toBookingFailedPayloadForBooking(
            Booking booking, ReserveSeatResponse reserve, String failureReason) {
        return new BookingFailedEventPayload(
                null, booking.getUserId(), null, null,
                booking.getFixtureId(), booking.getRequestedSeats(),
                failureReason, null,
                reserve != null ? reserve.homeTeamName()              : null,
                reserve != null ? reserve.awayTeamName()              : null,
                reserve != null ? reserve.currentScheduledStartTime() : null);
    }

    public SeatsReleaseEventPayload toSeatsReleasePayload(Booking booking, String reason) {
        return new SeatsReleaseEventPayload(
                booking.getFixtureId(),
                booking.getRequestedSeats(),
                booking.getId(),
                reason);
    }

    public BookingPaymentInitiatedEventPayload toPaymentInitiatedPayload(
            Booking booking, InitiatePaymentResponse payment, ReserveSeatResponse reserve) {
        return new BookingPaymentInitiatedEventPayload(
                booking.getId(),
                booking.getBookingAttemptId(),     // was attemptFutureIdOrNull — inlined here
                booking.getUserId(),
                null, null,
                booking.getFixtureId(),
                reserve.homeTeamName(),
                reserve.awayTeamName(),
                reserve.currentScheduledStartTime(),
                booking.getRequestedSeats(),
                booking.getTotalAmount(),
                payment.paymentIntentId(),
                booking.getExpiresAt());
    }

    public BookingConfirmedPayload toConfirmedPayload(BookingPendingCache bookingPendingCache, Booking booking) {
        return new BookingConfirmedPayload(
                bookingPendingCache.bookingId(),
                bookingPendingCache.userId(),
                bookingPendingCache.fixtureId(),
                bookingPendingCache.recipientEmail(),
                bookingPendingCache.recipientFullName(),
                bookingPendingCache.homeTeamName(),
                bookingPendingCache.awayTeamName(),
                bookingPendingCache.stadiumName(),
                bookingPendingCache.fixtureDateTime(),
                booking.getRequestedSeats(),
                booking.getTotalAmount(),
                booking.getCreatedAt()
        );
    }

    public BookingAttempt toNewBookingAttempt(Long userId, Long fixtureId, int requestedSeats) {
        return BookingAttempt.builder()
                .userId(userId)
                .fixtureId(fixtureId)
                .requestedSeats(requestedSeats)
                .status(BookingAttemptStatus.ATTEMPTED)
                .build();
    }
}