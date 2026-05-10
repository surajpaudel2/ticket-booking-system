package com.tbs.bookingservice.service;

import com.tbs.bookingservice.client.EventServiceClient;
import com.tbs.bookingservice.client.PaymentServiceClient;
import com.tbs.bookingservice.client.dto.InitiatePaymentRequest;
import com.tbs.bookingservice.client.dto.InitiatePaymentResponse;
import com.tbs.bookingservice.client.dto.ReserveSeatRequest;
import com.tbs.bookingservice.client.dto.ReserveSeatResponse;
import com.tbs.bookingservice.dto.cache.UserCacheDto;
import com.tbs.bookingservice.dto.request.InitiateBookingRequest;
import com.tbs.bookingservice.dto.response.InitiateBookingResponse;
import com.tbs.bookingservice.entity.Booking;
import com.tbs.bookingservice.entity.enums.BookingAttemptFailureReason;
import com.tbs.bookingservice.entity.enums.BookingStatus;
import com.tbs.bookingservice.exception.BookingException;
import com.tbs.bookingservice.messaging.payload.outbound.BookingFailedEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.BookingPaymentInitiatedEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.SeatsReleaseEventPayload;
import com.tbs.bookingservice.messaging.publisher.BookingEventPublisher;
import com.tbs.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Orchestrates the full booking initiation flow including async audit trail,
 * seat reservation, payment initiation, and event publishing.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingAttemptService bookingAttemptService;
    private final BookingCacheService bookingCacheService;
    private final EventServiceClient eventServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final BookingEventPublisher bookingEventPublisher;

    // Entry point — fires async attempt record, reserves seats, saves booking, initiates payment
    public InitiateBookingResponse initiateBooking(InitiateBookingRequest request) {
        log.info("Initiating booking: userId={}, fixtureId={}", request.getUserId(), request.getFixtureId());
        CompletableFuture<Long> attemptFuture = bookingAttemptService.saveBookingAttemptAsync(
                request.getUserId(), request.getFixtureId(), request.getRequestedSeats());
        ReserveSeatResponse reserveResponse = reserveSeatsOrFail(request, attemptFuture);
        fireAttemptSeatHintUpdate(attemptFuture, reserveResponse);
        Booking booking = saveBookingPending(request, reserveResponse);
        InitiatePaymentResponse paymentResponse = initiatePaymentOrFail(booking, reserveResponse, attemptFuture);
        storePaymentDetails(booking, paymentResponse);
        publishPaymentInitiatedEvent(booking, reserveResponse, paymentResponse);
        log.info("Booking initiated: bookingId={}", booking.getId());
        return buildInitiateBookingResponse(booking, paymentResponse);
    }

    // Calls event-service; on failure fires background attempt update and rethrows
    private ReserveSeatResponse reserveSeatsOrFail(InitiateBookingRequest request, CompletableFuture<Long> attemptFuture) {
        try {
            return eventServiceClient.reserveSeats(
                    new ReserveSeatRequest(request.getFixtureId(), request.getRequestedSeats(), request.getUserId()));
        } catch (BookingException ex) {
            BookingAttemptFailureReason reason = resolveFailureReason(ex);
            attemptFuture.thenAcceptAsync(id -> handleAttemptFailure(id, reason, request));
            throw ex;
        }
    }

    // Background: marks attempt failed; emails user only for INSUFFICIENT_SEATS
    private void handleAttemptFailure(Long attemptId, BookingAttemptFailureReason reason, InitiateBookingRequest request) {
        bookingAttemptService.updateAttemptAsFailed(attemptId, reason);
        if (reason != BookingAttemptFailureReason.INSUFFICIENT_SEATS) return;
        UserCacheDto user = bookingCacheService.findUserFromCache(request.getUserId()).orElse(null);
        bookingEventPublisher.publishBookingFailed(buildBookingFailedPayload(attemptId, reason, request, null, user));
    }

    // Builds and persists a PENDING booking with a 15-minute expiry window
    private Booking saveBookingPending(InitiateBookingRequest request, ReserveSeatResponse reserveResponse) {
        double totalAmount = reserveResponse.pricePerSeat() * request.getRequestedSeats();
        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .fixtureId(request.getFixtureId())
                .requestedSeats(request.getRequestedSeats())
                .pricePerSeat(reserveResponse.pricePerSeat())
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
        return bookingRepository.save(booking);
    }

    // Calls payment-service; on failure fires background compensation and rethrows
    private InitiatePaymentResponse initiatePaymentOrFail(Booking booking, ReserveSeatResponse reserveResponse,
            CompletableFuture<Long> attemptFuture) {
        try {
            return paymentServiceClient.initiatePayment(
                    new InitiatePaymentRequest(booking.getId(), booking.getTotalAmount(), "AUD", booking.getUserId()));
        } catch (Exception ex) {
            CompletableFuture.runAsync(() -> compensatePaymentFailure(booking, reserveResponse));
            throw new BookingException("Payment initiation failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Background: releases seats, marks booking FAILED, publishes failure event
    private void compensatePaymentFailure(Booking booking, ReserveSeatResponse reserveResponse) {
        bookingEventPublisher.publishSeatsRelease(new SeatsReleaseEventPayload(
                booking.getFixtureId(), booking.getRequestedSeats(), booking.getId(), "PAYMENT_FAILED"));
        booking.setStatus(BookingStatus.FAILED);
        bookingRepository.save(booking);
        UserCacheDto user = bookingCacheService.findUserFromCache(booking.getUserId()).orElse(null);
        bookingEventPublisher.publishBookingFailed(buildBookingFailedFromBooking(booking, reserveResponse, user));
    }

    // Persists payment intent id on booking and stores it in Redis with 15-min TTL
    private void storePaymentDetails(Booking booking, InitiatePaymentResponse paymentResponse) {
        booking.setPaymentIntentId(paymentResponse.paymentIntentId());
        bookingRepository.save(booking);
        bookingCacheService.storeBookingPending(booking.getId(), booking);
    }

    // Background: computes and stores seat hint on attempt without blocking main thread
    private void fireAttemptSeatHintUpdate(CompletableFuture<Long> attemptFuture, ReserveSeatResponse response) {
        attemptFuture.thenAcceptAsync(id -> {
            String hint = bookingAttemptService.computeSeatHint(response.availableSeatsRemaining(), response.seatsReserved());
            bookingAttemptService.updateAttemptWithSeatHint(id, hint);
        });
    }

    // Publishes payment initiated event so notification-service sends the checkout email
    private void publishPaymentInitiatedEvent(Booking booking, ReserveSeatResponse res, InitiatePaymentResponse payment) {
        UserCacheDto user = bookingCacheService.findUserFromCache(booking.getUserId()).orElse(null);
        String email = user != null ? user.email() : "";
        String name = user != null ? user.name() : "";
        bookingEventPublisher.publishPaymentInitiated(new BookingPaymentInitiatedEventPayload(
                booking.getId(), booking.getBookingAttemptId(), booking.getUserId(), email, name,
                booking.getFixtureId(), res.homeTeamName(), res.awayTeamName(),
                res.currentScheduledStartTime(), booking.getRequestedSeats(),
                booking.getTotalAmount(), payment.paymentIntentId(), booking.getExpiresAt()));
    }

    // Maps entity + payment response fields to the API response record
    private InitiateBookingResponse buildInitiateBookingResponse(Booking booking, InitiatePaymentResponse payment) {
        return new InitiateBookingResponse(
                booking.getId(), booking.getFixtureId(),
                payment.paymentIntentId(), payment.clientSecret(),
                booking.getTotalAmount(), booking.getRequestedSeats(),
                booking.getExpiresAt(), booking.getStatus().name());
    }

    // Derives failure reason from the HTTP status returned by event-service via Feign
    private BookingAttemptFailureReason resolveFailureReason(BookingException ex) {
        return ex.getStatus() == HttpStatus.NOT_FOUND
                ? BookingAttemptFailureReason.EVENT_NOT_FOUND
                : BookingAttemptFailureReason.INSUFFICIENT_SEATS;
    }

    // Builds the failed event payload for event-service failures (pre-booking, no booking entity yet)
    private BookingFailedEventPayload buildBookingFailedPayload(Long attemptId, BookingAttemptFailureReason reason,
            InitiateBookingRequest request, ReserveSeatResponse res, UserCacheDto user) {
        String email = user != null ? user.email() : "";
        String name = user != null ? user.name() : "";
        Integer available = res != null ? res.availableSeatsRemaining() : null;
        LocalDateTime startTime = res != null ? res.currentScheduledStartTime() : null;
        return new BookingFailedEventPayload(attemptId, request.getUserId(), email, name,
                request.getFixtureId(), request.getRequestedSeats(), reason.name(),
                available, res != null ? res.homeTeamName() : null, res != null ? res.awayTeamName() : null, startTime);
    }

    // Builds the failed event payload for payment-service failures (booking entity already exists)
    private BookingFailedEventPayload buildBookingFailedFromBooking(Booking booking, ReserveSeatResponse res, UserCacheDto user) {
        String email = user != null ? user.email() : "";
        String name = user != null ? user.name() : "";
        return new BookingFailedEventPayload(booking.getBookingAttemptId(), booking.getUserId(), email, name,
                booking.getFixtureId(), booking.getRequestedSeats(),
                BookingAttemptFailureReason.PAYMENT_FAILED.name(), null,
                res.homeTeamName(), res.awayTeamName(), res.currentScheduledStartTime());
    }
}
