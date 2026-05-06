package com.tbs.eventservice.entity;

import com.tbs.eventservice.entity.enums.ScanDirection;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_record")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter

// This class will help us in tracking the double scanned tickets and also things like if user is still in stadium then scan direction will only be in ENTRY, also, can track double scanned tickets and so on.
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The ticket that was scanned.
    // ManyToOne allows logging multiple scans (e.g., Entry, Exit, Re-entry, or accidental double-scans)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    private long attendedUserId;

    private String attendedUserName;

    @Enumerated(EnumType.STRING)
    private ScanDirection direction;

    @Column(nullable = false)
    private String gateNumber;

    // Automatically populated by Spring JPA Auditing
    @CreatedDate
    @Column(name = "scanned_at", nullable = false, updatable = false)
    private LocalDateTime scannedAt;

}