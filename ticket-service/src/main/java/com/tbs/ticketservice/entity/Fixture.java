package com.tbs.ticketservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

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
    Long id;

    @OneToOne
    Season season;

    @OneToMany // Because one fixture can be postponded multiple times.
    Postponded postponded;

    int nextSeat;

    int totalSeats;

    @OneToMany
    Set<Ticket> tickets; // To get total users who came to the fixtures, we can get how many booking were made from the tickets only as well

    LocalDateTime originalScheduledStartTime; // never changes — the first planned kickoff

    LocalDateTime currentScheduledStartTime;  // updates on every postponement

    LocalDateTime actualStartTime;            // set when fixture physically begins

    LocalDateTime actualEndTime;              // set when fixture physically ends

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    getTotalBookedSeats() {
//        will get this one by the logic.
    }



}
