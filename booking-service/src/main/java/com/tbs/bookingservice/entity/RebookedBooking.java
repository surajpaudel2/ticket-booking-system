package com.tbs.bookingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rebooked_booking")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
// Immutable audit record created when a booking is rebooked to a different fixture or seat.
// Consider adding fixtureId / seatNumber fields here to record what the booking was rebooked *to*.
public class RebookedBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ManyToOne: one booking can be rebooked multiple times (e.g. fixture postponed twice).
    // Each rebooking produces a new RebookedBooking row, giving us a full rebooking history.
    // This side owns the FK (booking_id) — Booking.rebookedBookings is the inverse (mappedBy = "booking").
    // FetchType.LAZY: loading a rebooking record should not pull the entire Booking graph.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // updatable = false: a rebooking timestamp is immutable — it records when the rebooking occurred, never changes.
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}