package com.tbs.eventservice.entity;

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
    Long id;

    @OneToOne
    Season season;

    @OneToMany(mappedBy = "fixture", cascade = CascadeType.ALL) // Because one fixture can be postponded multiple times.
    List<PostponedRecord> postponedRecords = new ArrayList<>();

    @OneToMany(mappedBy = "fixture", cascade = CascadeType.ALL)
    Set<Ticket> tickets = new HashSet<>(); // To get total users who came to the fixtures, we can get how many booking were made from the tickets only as well


    int nextSeat;

    int totalSeats;

    LocalDateTime originalScheduledStartTime; // never changes — the first planned kickoff

    LocalDateTime currentScheduledStartTime;  // updates on every postponement

    LocalDateTime actualStartTime;            // set when fixture physically begins

    LocalDateTime actualEndTime;              // set when fixture physically ends

    double pricePerSeat;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

//    getTotalBookedSeats() {
////        will get this one by the logic.
//    }



}
