package com.tbs.ticketservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "postponed_record")
public class PostpondedRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    LocalDateTime postponedAt; // when the fixture was postponed

    LocalDateTime postpondedFor;

    String postpondedReason;

    @ManyToOne
    Fixture fixture;
}
