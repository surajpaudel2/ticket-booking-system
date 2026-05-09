package com.tbs.eventservice.entity;

import com.tbs.eventservice.entity.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ticket")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
// Represents a seat reservation for a specific fixture.
// One ticket belongs to one fixture and one user, but can accumulate multiple scan events over its lifetime.
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // userId from user-service — no FK constraint since the user lives in a separate microservice.
    // Validated at the application layer when the ticket is created.
    // Long (not int) to be consistent with how JPA generates IDs and to avoid overflow risk.
    @Column(name = "booked_by_id", nullable = false)
    private Long bookedById;

    // Stored as STRING so the column value is human-readable in the DB (e.g. "ACTIVE", "USED", "CANCELLED").
    // Avoids fragile ordinal-based mapping that breaks if the enum order ever changes.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus ticketStatus;

    // ManyToOne: many tickets can belong to the same fixture (one seat per ticket, many seats per fixture).
    // This side owns the FK (fixture_id) — Fixture.tickets is the inverse (mappedBy = "fixture").
    // FetchType.LAZY: loading a ticket for status checks or scanning should not pull the entire Fixture graph.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;

    // OneToMany: one ticket can be scanned multiple times — entry, exit, re-entry, or accidental double-scans.
    // mappedBy = "ticket": AttendanceRecord owns the FK; this side is the inverse read-only view.
    // CascadeType.ALL: scanning records are lifecycle-bound to the ticket — if a ticket is deleted, its scan history goes with it.
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AttendanceRecord> attendanceRecords = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}