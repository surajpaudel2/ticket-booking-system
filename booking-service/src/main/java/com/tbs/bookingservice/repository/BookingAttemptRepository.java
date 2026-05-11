package com.tbs.bookingservice.repository;

import com.tbs.bookingservice.entity.BookingAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tbs.bookingservice.entity.enums.BookingAttemptStatus;

import java.time.LocalDateTime;
import java.util.List;

/** JPA repository for BookingAttempt persistence. */
public interface BookingAttemptRepository extends JpaRepository<BookingAttempt, Long> {

    // Used by nudge scheduler — finds ATTEMPTED attempts older than 30 min with no nudge sent yet
    List<BookingAttempt> findAllByStatusAndNudgeSentAtIsNullAndCreatedAtBefore(
            BookingAttemptStatus status, LocalDateTime threshold);
}
