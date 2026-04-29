package com.tbs.ticketservice.entity;

import jakarta.persistence.*;

@Entity
@Table (name = "ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
