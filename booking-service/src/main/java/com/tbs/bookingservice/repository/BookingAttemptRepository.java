package com.tbs.bookingservice.repository;

import com.tbs.bookingservice.entity.BookingAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for BookingAttempt persistence. */
public interface BookingAttemptRepository extends JpaRepository<BookingAttempt, Long> {}
