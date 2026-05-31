package com.tbs.bookingservice.service.impl;

import com.tbs.bookingservice.client.EventServiceClient;
import com.tbs.bookingservice.client.PaymentServiceClient;
import com.tbs.bookingservice.client.dto.request.InitiatePaymentRequest;
import com.tbs.bookingservice.client.dto.response.InitiatePaymentResponse;
import com.tbs.bookingservice.client.dto.request.ReserveSeatRequest;
import com.tbs.bookingservice.client.dto.response.ReserveSeatResponse;
import com.tbs.bookingservice.mapper.BookingCacheMapper;
import com.tbs.bookingservice.mapper.BookingMapper;
import com.tbs.bookingservice.client.util.FeignResponseUtils;
import com.tbs.bookingservice.dto.request.InitiateBookingRequest;
import com.tbs.bookingservice.dto.response.InitiateBookingResponse;
import com.tbs.bookingservice.entity.Booking;
import com.tbs.bookingservice.entity.enums.BookingAttemptFailureReason;
import com.tbs.bookingservice.entity.enums.BookingStatus;
import com.tbs.bookingservice.entity.enums.OutboxEventType;
import com.tbs.bookingservice.exception.BookingException;
import com.tbs.bookingservice.messaging.payload.outbound.BookingFailedEventPayload;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingAttemptService bookingAttemptService;
    private final BookingCacheService bookingCacheService;
    private final EventServiceClient eventServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final OutboxService outboxService;
    private final BookingEventPublisher bookingEventPublisher;
    private final BookingMapper bookingMapper;
    @Qualifier("bookingAsyncExecutor")
    private final Executor bookingAsyncExecutor;
    private final BookingCacheMapper bookingCacheMapper;


    @Override
    public InitiateBookingResponse initiateBooking(InitiateBookingRequest request) throws ExecutionException, InterruptedException {
        // save booking attempted in pending in background, to know user tried to book and to have a record to link the outbox events to, even if the booking ultimately fails (e.g. due to payment failure) and never transitions to CONFIRMED
        CompletableFuture<Long> attemptFuture = bookingAttemptService
                .saveBookingAttemptAsync(request.getUserId(), request.getFixtureId(), request.getRequestedSeats());

        // Deduct seats in the event-service by the main thread
        ReserveSeatResponse reserveResponse = reserveSeatsOrFail(request, attemptFuture);

        // Assuming seats is deducted from the event-service, but the whole process hasn't been completed yet, so if the payment fails then to send the seat hint update for the future.
        fireAttemptSeatHintUpdate(attemptFuture, reserveResponse, request.getRequestedSeats());

        // Create booking in PENDING state, to be later updated to CONFIRMED or FAILED based also,  update the bookingAttemptId to send the nudge emails.
        Booking booking = saveBookingPending(attemptFuture.get(), request, reserveResponse);

        // Connect with payment-service to create a payment intent. If this fails, it will trigger the compensating logic as well in the background to make Booking as failed, and to release the Seats in the Outbox db.
        InitiatePaymentResponse paymentResponse = initiatePaymentOrFail(booking, reserveResponse, attemptFuture);

        // Saves in DB with Payment Intent, also, in Cache with Booking as Pending Status, for the confirmation, which is handled by PaymentResultService class.
        // TODO : Since we are soley relying in Cache[REDIS] for now, when the confirmation is done, don't only trust in the Cache, but also save in the DB as well.
        finaliseInitiateBooking(booking, paymentResponse, reserveResponse, attemptFuture);

        return bookingMapper.toInitiateBookingResponse(booking, paymentResponse);
    }

    private ReserveSeatResponse reserveSeatsOrFail(InitiateBookingRequest request,
                                                   CompletableFuture<Long> attemptFuture) {
        try {
            ReserveSeatRequest reserveRequest = bookingMapper.toReserveSeatRequest(request);
            return FeignResponseUtils.unwrap(eventServiceClient.reserveSeats(reserveRequest));
        } catch (BookingException ex) {
            log.warn("Seat reservation failed — compensation triggered: {}", ex.getMessage());
            attemptFuture.thenAcceptAsync(
                    id -> handleAttemptFailureInBackground(id, ex, null, request.getUserId(), request.getFixtureId()),
                    bookingAsyncExecutor);
            throw ex;
        }
    }

    private void handleAttemptFailureInBackground(Long attemptId, BookingException ex,
                                                  ReserveSeatResponse reserveResponse,
                                                  Long userId, Long fixtureId) {
        BookingAttemptFailureReason reason = resolveFailureReason(ex.getStatus());
        bookingAttemptService.updateAttemptAsFailed(attemptId, reason);
        BookingFailedEventPayload payload = bookingMapper.toBookingFailedPayloadForAttempt(
                attemptId, userId, fixtureId, reason, reserveResponse);
        outboxService.saveEvent(OutboxEventType.BOOKING_FAILED, payload, null, attemptId);
    }

    private void fireAttemptSeatHintUpdate(CompletableFuture<Long> attemptFuture,
                                           ReserveSeatResponse response, int requestedSeats) {
        attemptFuture.thenAcceptAsync(id -> {
            String hint = bookingAttemptService.computeSeatHint(response.availableSeatsRemaining(), requestedSeats);
            bookingAttemptService.updateAttemptWithSeatHint(id, hint);
        }, bookingAsyncExecutor);
    }

    private Booking saveBookingPending(Long bookingAttemptId, InitiateBookingRequest request, ReserveSeatResponse reserveResponse) {
        Booking booking = bookingMapper.toPendingBooking(bookingAttemptId, request, reserveResponse);
        Booking saved = bookingRepository.save(booking);
        log.info("Booking PENDING saved id={}", saved.getId());
        return saved;
    }

    private InitiatePaymentResponse initiatePaymentOrFail(Booking booking,
                                                          ReserveSeatResponse reserveResponse,
                                                          CompletableFuture<Long> attemptFuture) {
        try {
            InitiatePaymentRequest paymentRequest = bookingMapper.toInitiatePaymentRequest(booking);
            return FeignResponseUtils.unwrap(paymentServiceClient.initiatePaymentIntent(paymentRequest));
        } catch (BookingException ex) {
            log.warn("Payment failed for bookingId={} — triggering compensation", booking.getId());
            triggerPaymentCompensationInBackground(booking, reserveResponse, attemptFuture);
            throw ex;
        }
    }

    private void triggerPaymentCompensationInBackground(Booking booking,
                                                        ReserveSeatResponse reserveResponse,
                                                        CompletableFuture<Long> attemptFuture) {
        CompletableFuture.runAsync(() -> {
            outboxService.saveEvent(OutboxEventType.SEATS_RELEASE,
                    bookingMapper.toSeatsReleasePayload(booking, "PAYMENT_FAILED"),
                    booking.getId(), null);
            booking.setStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);
            attemptFuture.thenAcceptAsync(
                    id -> bookingAttemptService.updateAttemptAsFailed(id, BookingAttemptFailureReason.PAYMENT_FAILED),
                    bookingAsyncExecutor);
            outboxService.saveEvent(OutboxEventType.BOOKING_FAILED,
                    bookingMapper.toBookingFailedPayloadForBooking(booking, reserveResponse, "PAYMENT_FAILED"),
                    booking.getId(), null);
        }, bookingAsyncExecutor);
    }

    private void finaliseInitiateBooking(Booking booking, InitiatePaymentResponse paymentResponse,
                                         ReserveSeatResponse reserveResponse,
                                         CompletableFuture<Long> attemptFuture) {
        booking.setPaymentIntentId(paymentResponse.paymentIntentId());
        bookingRepository.save(booking);

        bookingCacheService.storeBookingPending(bookingCacheMapper.toBookingPendingCache(booking, reserveResponse));
        attemptFuture.thenAcceptAsync(
                id -> bookingAttemptService.updateAttemptAsProceeded(id, booking.getId()),
                bookingAsyncExecutor);

        // because as soon as the payment is initiated we want to send the email to the user without waiting for the outbox scheduler to pick up the event
        bookingEventPublisher.publishPaymentInitiatedForBooking(
                bookingMapper.toPaymentInitiatedPayload(booking, paymentResponse, reserveResponse));
    }

    private BookingAttemptFailureReason resolveFailureReason(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> BookingAttemptFailureReason.EVENT_NOT_FOUND;
            case CONFLICT  -> BookingAttemptFailureReason.INSUFFICIENT_SEATS;
            default        -> BookingAttemptFailureReason.SEAT_RESERVATION_FAILED;
        };    }
}