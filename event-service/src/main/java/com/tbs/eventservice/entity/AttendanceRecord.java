package com.tbs.eventservice.entity;

import com.tbs.eventservice.entity.enums.ScanDirection;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_record")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
// Tracks every scan event against a ticket — entry, exit, re-entry, or accidental double-scans.
// Multiple records per ticket are intentional: they form a scan history, not a single attendance flag.
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ManyToOne: one ticket can be scanned multiple times (Entry, Exit, Re-entry, double-scan).
    // Each scan produces a new AttendanceRecord row, giving us a full audit trail per ticket.
    // FetchType.LAZY: we never need the full Ticket graph just to log or query a scan event.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    // Denormalized from user-service at scan time — no FK constraint since user lives in another service.
    // Stored here so the scan record is self-contained even if the user record changes later.
    private Long attendedUserId;

    private String attendedUserName;

    @Enumerated(EnumType.STRING)
    private ScanDirection direction;

    @Column(nullable = false)
    private String gateNumber;

    // @CreationTimestamp: automatically set by Hibernate on insert — no manual assignment needed.
    // updatable = false: scannedAt is immutable once written; a scan event cannot be backdated.
    @CreationTimestamp
    @Column(name = "scanned_at", nullable = false, updatable = false)
    private LocalDateTime scannedAt;
}