package com.tbs.eventservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "postponed_record")
public class PostponedRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    LocalDateTime postponedAt; // when the fixture was postponed

    LocalDateTime postpondedFor;

    String postpondedReason;

    @ManyToOne
    @JoinColumn(name = "fixture_id", nullable = false)
    Fixture fixture;
}
