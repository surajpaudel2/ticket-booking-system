package com.tbs.bookingservice.repository;

import com.tbs.bookingservice.entity.Booking;
import com.tbs.bookingservice.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/** JPA repository for Booking persistence. */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Used by the expiry scheduler to find PENDING bookings whose payment window has closed
    List<Booking> findAllByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime dateTime);
}
