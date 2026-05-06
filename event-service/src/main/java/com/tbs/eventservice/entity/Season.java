package com.tbs.eventservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "season")
@NoArgsConstructor @AllArgsConstructor
@Getter  @Setter
public class Season {

    @Id
    private Long id;

    @OneToOne
    Fixture fixture;

    private LocalDateTime expectedStartDateTime;

    private LocalDateTime expectedEndDateTime;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
