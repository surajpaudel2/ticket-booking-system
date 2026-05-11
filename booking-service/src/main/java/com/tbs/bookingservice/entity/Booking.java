package com.tbs.bookingservice.entity;

import com.tbs.bookingservice.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Core booking aggregate for the booking-service.
// Ticket details (seat number, fixture, price) live in event-service and are fetched via Feign client when needed.
// Whether the user is a season ticket holder is determined at booking time using userId against event-service.
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // userId from user-service — no FK constraint since the user lives in a separate microservice.
    // Validated at the application layer when the booking is created.
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long fixtureId;

    private Long bookingAttemptId;

    @Column(nullable = false)
    private int requestedSeats;

    @Column(nullable = false)
    private double pricePerSeat;

    @Column(nullable = false)
    private double totalAmount;

    private String paymentIntentId;

    private LocalDateTime expiresAt;

    // OneToMany: one booking can have multiple cancellation records (e.g. retry attempts, partial cancellations).
    // mappedBy = "booking": CancelledBooking owns the FK; this is the inverse read-only view.
    // CascadeType.ALL + LAZY: cancellation records are lifecycle-bound to the booking — deleted with it, loaded on demand.
    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CancelledBooking> cancelledBookings = new ArrayList<>();

    // OneToMany: one booking can be rebooked multiple times (e.g. fixture postponed twice in a season).
    // mappedBy = "booking": RebookedBooking owns the FK; this is the inverse read-only view.
    // CascadeType.ALL + LAZY: rebooking records are lifecycle-bound to the booking — deleted with it, loaded on demand.
    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RebookedBooking> rebookedBookings = new ArrayList<>();

    // Stored as STRING so the column value is human-readable (e.g. "PENDING", "CONFIRMED", "CANCELLED").
    // Avoids fragile ordinal-based mapping that breaks if the enum order ever changes.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    // Timestamp set when the first reminder email is sent (at ~5 min mark).
    // Null means reminder 1 has not been sent yet.
    // Reminder scheduler checks this to prevent duplicate sends.
    private LocalDateTime reminder1SentAt;

    // Timestamp set when the second reminder email is sent (at ~10 min mark).
    // Null means reminder 2 has not been sent yet.
    // Reminder scheduler checks this to prevent duplicate sends.
    private LocalDateTime reminder2SentAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}