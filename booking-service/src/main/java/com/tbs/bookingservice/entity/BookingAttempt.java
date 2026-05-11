package com.tbs.bookingservice.entity;

import com.tbs.bookingservice.entity.enums.BookingAttemptFailureReason;
import com.tbs.bookingservice.entity.enums.BookingAttemptStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_attempt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long fixtureId;

    @Column(nullable = false)
    private int requestedSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingAttemptStatus status;

    @Enumerated(EnumType.STRING)
    private BookingAttemptFailureReason failureReason;

    private String seatHint;

    private Long bookingId;

    // Timestamp set when the nudge email is sent to this customer.
    // Null means nudge has never been sent.
    // The nudge scheduler checks this field to prevent duplicate nudge emails.
    // Only set if status = ATTEMPTED, createdAt < now - 30 min, and seatHint is not null.
    private LocalDateTime nudgeSentAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
