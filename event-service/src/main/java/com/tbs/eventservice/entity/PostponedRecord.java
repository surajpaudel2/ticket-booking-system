package com.tbs.eventservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "postponed_record")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
// Immutable audit log of every postponement event for a fixture.
// A fixture can be postponed multiple times, so each postponement gets its own row.
public class PostponedRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // When the postponement decision was made — set at creation, never updated.
    @Column(nullable = false, updatable = false)
    private LocalDateTime postponedAt;

    // The new scheduled datetime the fixture was moved to.
    @Column(nullable = false)
    private LocalDateTime postponedFor;

    @Column(nullable = false)
    private String postponedReason;

    // ManyToOne: one fixture can accumulate many postponement records over its lifetime.
    // This side owns the FK (fixture_id) — Fixture.postponedRecords is the inverse (mappedBy).
    // FetchType.LAZY: loading a postponement log entry does not require loading the full Fixture graph.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;
}