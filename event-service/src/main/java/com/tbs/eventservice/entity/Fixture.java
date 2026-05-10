package com.tbs.eventservice.entity;

import com.tbs.eventservice.entity.enums.FixtureClassification;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// In future can implement the feature like, expose that sort of fixture to the client for booking which are bookable only.

@Entity
@Table(name = "fixture")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fixture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String homeTeamName;

    @Column(length = 100, nullable = false)
    private String awayTeamName;

    @Column(length = 100, nullable = false)
    private String stadiumName;

    @Column(length = 100, nullable = false)
    private FixtureClassification classification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private Season season;

    @OneToMany(mappedBy = "fixture", cascade = CascadeType.ALL, fetch = FetchType.LAZY) // Because one fixture can be postponed multiple times.
    private List<PostponedRecord> postponedRecords = new ArrayList<>();

    @OneToMany(mappedBy = "fixture", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Ticket> tickets = new HashSet<>(); // To get total users who came to the fixtures, we can get how many bookings were made from the tickets only as well

    private int nextSeat;

    private int totalSeats;

    @Column(nullable = false)
    private int availableSeats;

    @Version
    private Long version;

    private LocalDateTime originalScheduledStartTime; // never changes — the first planned kickoff

    private LocalDateTime currentScheduledStartTime;  // updates on every postponement

    private LocalDateTime actualStartTime;            // set when fixture physically begins

    private LocalDateTime actualEndTime;              // set when fixture physically ends

    private double pricePerSeat;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}