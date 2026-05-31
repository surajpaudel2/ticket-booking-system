package com.tbs.bookingservice.mapper;

import com.tbs.bookingservice.client.dto.response.ReserveSeatResponse;
import com.tbs.bookingservice.dto.cache.BookingPendingCache;
import com.tbs.bookingservice.dto.request.InitiateBookingRequest;
import com.tbs.bookingservice.entity.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class BookingCacheMapper {

    private final ObjectMapper objectMapper;

    public BookingPendingCache toBookingPendingCache(Booking booking, ReserveSeatResponse reserveSeatResponse) {
        return new  BookingPendingCache(
            booking.getId(),
            booking.getFixtureId(),
            booking.getUserId(),
            booking.getRecipientFullName(),
            booking.getRecipientEmail(),
            booking.getRequestedSeats(),
            booking.getTotalAmount(),
            booking.getPaymentIntentId(),
            reserveSeatResponse.homeTeamName(),
            reserveSeatResponse.awayTeamName(),
            reserveSeatResponse.stadiumName(),
            reserveSeatResponse.currentScheduledStartTime()
        );
    }

    public BookingPendingCache toBookingPendingCache(Object cached) {
        return objectMapper.convertValue(cached, BookingPendingCache.class);
    }

}
