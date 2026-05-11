package com.tbs.bookingservice.service;

/** Manages Redis cache operations for booking-service state windows. */
public interface BookingCacheService {

    /** Stores pending booking state in Redis with a 15-minute TTL. */
    void storeBookingPending(Long bookingId, Object data);

    /** Removes the pending booking key from Redis. */
    void evictBookingPending(Long bookingId);

    /** Returns true if the pending booking key exists in Redis, false otherwise. */
    boolean isBookingPendingInCache(Long bookingId);
}
