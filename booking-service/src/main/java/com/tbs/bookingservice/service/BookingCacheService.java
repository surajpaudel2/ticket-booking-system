package com.tbs.bookingservice.service;

import com.tbs.bookingservice.dto.cache.BookingPendingCache;

import java.util.Optional;

/** Manages Redis cache operations for booking-service state windows. */
public interface BookingCacheService {

    /** Stores pending booking state in Redis with a 15-minute TTL. */
    void storeBookingPending(BookingPendingCache bookingPendingCache);

    /** Removes the pending booking key from Redis. */
    void evictBookingPending(Long bookingId);

    Optional<BookingPendingCache> getBookingPending(Long bookingId);

    /** Returns true if the pending booking key exists in Redis, false otherwise. */
    boolean isBookingPendingInCache(Long bookingId);
}
