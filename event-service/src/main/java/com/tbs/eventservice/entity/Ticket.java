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

@Entity
@Table (name = "ticket")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int bookedById; // Representing the userId of the user in the ticket.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TicketStatus ticketStatus;

    @ManyToOne
    @JoinColumn(name = "fixture_id", nullable = false)
    Fixture fixture;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
