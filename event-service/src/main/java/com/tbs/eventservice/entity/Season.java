package com.tbs.eventservice.entity;

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

/*
 * Represents a football season — the primary container for all fixtures and season ticket holders
 * within a defined time period (e.g. 2024/25 A-League season).
 *
 * Currently models a generic season, but is designed to be extended in the future to support
 * distinct competition types such as:
 *   - League season (round-robin, points table, relegation)
 *   - Cup competition (knockout, single-leg or two-leg ties)
 *   - Friendly tournament (pre-season, invitational)
 *   - International window (World Cup qualifiers, continental championships)
 *
 * A Season owns its Fixtures and SeasonTicketHolders — if a season is removed, all associated
 * records are removed with it (CascadeType.ALL + orphanRemoval).
 */

@Entity
@Table(name = "season")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Fixture> fixtures = new ArrayList<>();

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SeasonTicketHolder> seasonTicketHolders = new ArrayList<>();

    private LocalDateTime expectedStartDateTime;

    private LocalDateTime expectedEndDateTime;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}