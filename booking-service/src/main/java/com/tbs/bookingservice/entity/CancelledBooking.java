package com.tbs.bookingservice.entity;

import com.tbs.bookingservice.entity.enums.BookingCancellationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cancelled_booking")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
// Immutable audit record created when a booking is cancelled.
// Ticket details (seat number, fixture, etc.) are fetched via Feign client from event-service when needed.
public class CancelledBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // camelCase field name — @Column(name) handles the snake_case column in the DB separately.
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    // Stored as STRING so the column value is human-readable (e.g. "USER_REQUESTED", "ADMIN_OVERRIDE").
    // Avoids fragile ordinal-based mapping that breaks if the enum order ever changes.
    @Enumerated(EnumType.STRING)
    private BookingCancellationType bookingCancellationType;

    // updatable = false: a cancellation timestamp is immutable — it records when the event occurred, never changes.
    @CreationTimestamp
    @Column(name = "cancelled_at", nullable = false, updatable = false)
    private LocalDateTime cancelledAt;

    // ManyToOne: one booking can theoretically have multiple cancellation attempts logged (e.g. retry scenarios),
    // but more importantly this gives us the full cancellation history traceable back to the original booking.
    // This side owns the FK (booking_id) — Booking.cancelledBookings is the inverse (mappedBy = "booking").
    // FetchType.LAZY: loading a cancellation record should not pull the entire Booking graph.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
}