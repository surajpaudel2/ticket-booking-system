package com.tbs.bookingservice.service.impl;

import com.tbs.bookingservice.client.EventServiceClient;
import com.tbs.bookingservice.client.PaymentServiceClient;
import com.tbs.bookingservice.client.dto.InitiatePaymentRequest;
import com.tbs.bookingservice.client.dto.InitiatePaymentResponse;
import com.tbs.bookingservice.client.dto.ReserveSeatRequest;
import com.tbs.bookingservice.client.dto.ReserveSeatResponse;
import com.tbs.bookingservice.dto.request.InitiateBookingRequest;
import com.tbs.bookingservice.dto.response.InitiateBookingResponse;
import com.tbs.bookingservice.entity.Booking;
import com.tbs.bookingservice.entity.enums.BookingAttemptFailureReason;
import com.tbs.bookingservice.entity.enums.BookingStatus;
import com.tbs.bookingservice.entity.enums.OutboxEventType;
import com.tbs.bookingservice.exception.BookingException;
import com.tbs.bookingservice.messaging.payload.outbound.BookingFailedEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.BookingPaymentInitiatedEventPayload;
import com.tbs.bookingservice.messaging.payload.outbound.SeatsReleaseEventPayload;
import com.tbs.bookingservice.messaging.publisher.BookingEventPublisher;
import com.tbs.bookingservice.repository.BookingRepository;
import com.tbs.bookingservice.service.BookingAttemptService;
import com.tbs.bookingservice.service.BookingCacheService;
import com.tbs.bookingservice.service.BookingService;
import com.tbs.bookingservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Orchestrates the full booking initiation flow. Coordinates async audit trail,
 * seat reservation via Feign, payment initiation via Feign, outbox event saving,
 * Redis TTL window, and direct event publishing for time-critical notifications.
 */
@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingAttemptService bookingAttemptService;
    private final BookingCacheService bookingCacheService;
    private final EventServiceClient eventServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final OutboxService outboxService;
    private final BookingEventPublisher bookingEventPublisher;
    private final Executor bookingAsyncExecutor;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            BookingAttemptService bookingAttemptService,
            BookingCacheService bookingCacheService,
            EventServiceClient eventServiceClient,
            PaymentServiceClient paymentServiceClient,
            OutboxService outboxService,
            BookingEventPublisher bookingEventPublisher,
            @Qualifier("bookingAsyncExecutor") Executor bookingAsyncExecutor) {
        this.bookingRepository = bookingRepository;
        this.bookingAttemptService = bookingAttemptService;
        this.bookingCacheService = bookingCacheService;
        this.eventServiceClient = eventServiceClient;
        this.paymentServiceClient = paymentServiceClient;
        this.outboxService = outboxService;
        this.bookingEventPublisher = bookingEventPublisher;
        this.bookingAsyncExecutor = bookingAsyncExecutor;
    }

    // Entry point — fires async audit, reserves seats, saves booking, initiates payment, finalises
    @Override
    public InitiateBookingResponse initiateBooking(InitiateBookingRequest request) {
        CompletableFuture<Long> attemptFuture = bookingAttemptService.saveBookingAttemptAsync(
                request.getUserId(), request.getFixtureId(), request.getRequestedSeats());

        ReserveSeatResponse reserveResponse = reserveSeatsOrFail(request, attemptFuture);

        fireAttemptSeatHintUpdate(attemptFuture, reserveResponse, request.getRequestedSeats());

        Booking booking = saveBookingPending(request, reserveResponse);

        InitiatePaymentResponse paymentResponse = initiatePaymentOrFail(booking, reserveResponse, attemptFuture);

        finaliseBooking(booking, paymentResponse, reserveResponse, attemptFuture);

        return buildResponse(booking, paymentResponse);
    }

    // Calls event-service; on failure fires background attempt failure handling and rethrows
    private ReserveSeatResponse reserveSeatsOrFail(InitiateBookingRequest request,
                                                    CompletableFuture<Long> attemptFuture) {
        try {
            return eventServiceClient.reserveSeats(
                    new ReserveSeatRequest(request.getFixtureId(), request.getRequestedSeats(), request.getUserId()));
        } catch (BookingException ex) {
            log.warn("Seat reservation failed — compensation triggered: {}", ex.getMessage());
            attemptFuture.thenAcceptAsync(
                    id -> handleAttemptFailureInBackground(id, ex, null, request.getUserId(), request.getFixtureId()),
                    bookingAsyncExecutor);
            throw ex;
        }
    }

    // Determines failure reason and saves BOOKING_FAILED outbox event; runs in background thread
    private void handleAttemptFailureInBackground(Long attemptId, BookingException ex,
                                                   ReserveSeatResponse reserveResponse,
                                                   Long userId, Long fixtureId) {
        BookingAttemptFailureReason reason = resolveFailureReason(ex.getStatus());
        bookingAttemptService.updateAttemptAsFailed(attemptId, reason);
        BookingFailedEventPayload payload = buildBookingFailedPayloadForAttempt(
                attemptId, userId, fixtureId, reason, reserveResponse);
        outboxService.saveEvent(OutboxEventType.BOOKING_FAILED, payload, null, attemptId);
    }

    // Fires non-blocking thenAcceptAsync to update seat hint without holding main thread
    private void fireAttemptSeatHintUpdate(CompletableFuture<Long> attemptFuture,
                                            ReserveSeatResponse response, int requestedSeats) {
        attemptFuture.thenAcceptAsync(id -> {
            String hint = bookingAttemptService.computeSeatHint(response.availableSeatsRemaining(), requestedSeats);
            bookingAttemptService.updateAttemptWithSeatHint(id, hint);
        }, bookingAsyncExecutor);
    }

    // Builds and persists the PENDING booking with 15-minute expiry window
    private Booking saveBookingPending(InitiateBookingRequest request, ReserveSeatResponse reserveResponse) {
        double total = reserveResponse.pricePerSeat() * request.getRequestedSeats();
        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .fixtureId(request.getFixtureId())
                .requestedSeats(request.getRequestedSeats())
                .pricePerSeat(reserveResponse.pricePerSeat())
                .totalAmount(total)
                .status(BookingStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
        Booking saved = bookingRepository.save(booking);
        log.info("Booking PENDING saved id={}", saved.getId());
        return saved;
    }

    // Calls payment-service; on failure triggers background compensation and rethrows
    private InitiatePaymentResponse initiatePaymentOrFail(Booking booking,
                                                           ReserveSeatResponse reserveResponse,
                                                           CompletableFuture<Long> attemptFuture) {
        try {
            return paymentServiceClient.initiatePayment(
                    new InitiatePaymentRequest(booking.getId(), booking.getTotalAmount(), "GBP", booking.getUserId()));
        } catch (BookingException ex) {
            log.warn("Payment failed for bookingId={} — triggering compensation", booking.getId());
            triggerPaymentCompensationInBackground(booking, reserveResponse, attemptFuture);
            throw ex;
        }
    }

    // Runs all payment failure compensation steps in a background thread
    private void triggerPaymentCompensationInBackground(Booking booking,
                                                         ReserveSeatResponse reserveResponse,
                                                         CompletableFuture<Long> attemptFuture) {
        CompletableFuture.runAsync(() -> {
            outboxService.saveEvent(OutboxEventType.SEATS_RELEASE,
                    new SeatsReleaseEventPayload(booking.getFixtureId(), booking.getRequestedSeats(),
                            booking.getId(), "PAYMENT_FAILED"),
                    booking.getId(), null);
            booking.setStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);
            attemptFuture.thenAcceptAsync(
                    id -> bookingAttemptService.updateAttemptAsFailed(id, BookingAttemptFailureReason.PAYMENT_FAILED),
                    bookingAsyncExecutor);
            outboxService.saveEvent(OutboxEventType.BOOKING_FAILED,
                    buildBookingFailedPayloadForBooking(booking, reserveResponse, "PAYMENT_FAILED"),
                    booking.getId(), null);
        }, bookingAsyncExecutor);
    }

    // Stamps payment intent, caches pending window, updates attempt, and directly publishes payment event
    private void finaliseBooking(Booking booking, InitiatePaymentResponse paymentResponse,
                                  ReserveSeatResponse reserveResponse,
                                  CompletableFuture<Long> attemptFuture) {
        booking.setPaymentIntentId(paymentResponse.paymentIntentId());
        bookingRepository.save(booking);
        bookingCacheService.storeBookingPending(booking.getId(), booking);
        attemptFuture.thenAcceptAsync(
                id -> bookingAttemptService.updateAttemptAsProceeded(id, booking.getId()),
                bookingAsyncExecutor);
        bookingEventPublisher.publishPaymentInitiated(
                buildPaymentInitiatedPayload(booking, paymentResponse, reserveResponse));
    }

    // Maps HttpStatus from BookingException to the matching BookingAttemptFailureReason
    private BookingAttemptFailureReason resolveFailureReason(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> BookingAttemptFailureReason.EVENT_NOT_FOUND;
            case CONFLICT -> BookingAttemptFailureReason.INSUFFICIENT_SEATS;
            default -> BookingAttemptFailureReason.PAYMENT_FAILED;
        };
    }

    // Builds failed event payload from attempt-level data (no booking yet)
    private BookingFailedEventPayload buildBookingFailedPayloadForAttempt(
            Long attemptId, Long userId, Long fixtureId,
            BookingAttemptFailureReason reason, ReserveSeatResponse reserve) {
        boolean isEventNotFound = reason == BookingAttemptFailureReason.EVENT_NOT_FOUND;

        return new BookingFailedEventPayload(
                attemptId,
                userId,
                null,
                null,
                fixtureId,
                0,
                reason.name(),
                null,
                !isEventNotFound && reserve != null ? reserve.homeTeamName()             : null,
                !isEventNotFound && reserve != null ? reserve.awayTeamName()             : null,
                !isEventNotFound && reserve != null ? reserve.currentScheduledStartTime() : null
        );
    }

    // Builds failed event payload from booking-level data (booking already persisted)
    private BookingFailedEventPayload buildBookingFailedPayloadForBooking(
            Booking booking, ReserveSeatResponse reserve, String failureReason) {
        return new BookingFailedEventPayload(
                null, booking.getUserId(), null, null,
                booking.getFixtureId(), booking.getRequestedSeats(),
                failureReason, null,
                reserve != null ? reserve.homeTeamName() : null,
                reserve != null ? reserve.awayTeamName() : null,
                reserve != null ? reserve.currentScheduledStartTime() : null);
    }

    // Builds the payment initiated payload for the direct publish to notification-service
    private BookingPaymentInitiatedEventPayload buildPaymentInitiatedPayload(
            Booking booking, InitiatePaymentResponse payment, ReserveSeatResponse reserve) {
        return new BookingPaymentInitiatedEventPayload(
                booking.getId(), attemptFutureIdOrNull(booking), booking.getUserId(),
                null, null,
                booking.getFixtureId(),
                reserve.homeTeamName(), reserve.awayTeamName(), reserve.currentScheduledStartTime(),
                booking.getRequestedSeats(), booking.getTotalAmount(),
                payment.paymentIntentId(), booking.getExpiresAt());
    }

    // Returns the bookingAttemptId from the booking if already set, null otherwise
    private Long attemptFutureIdOrNull(Booking booking) {
        return booking.getBookingAttemptId();
    }

    // Assembles the client-facing response with Stripe checkout credentials
    private InitiateBookingResponse buildResponse(Booking booking, InitiatePaymentResponse payment) {
        return new InitiateBookingResponse(
                booking.getId(), booking.getFixtureId(),
                payment.paymentIntentId(), payment.clientSecret(),
                booking.getTotalAmount(), booking.getRequestedSeats(),
                booking.getExpiresAt(), booking.getStatus().name());
    }
}
