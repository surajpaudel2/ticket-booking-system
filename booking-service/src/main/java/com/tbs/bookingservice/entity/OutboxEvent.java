package com.tbs.bookingservice.entity;

import com.tbs.bookingservice.entity.enums.OutboxEventStatus;
import com.tbs.bookingservice.entity.enums.OutboxEventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_event")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
// Outbox pattern entity — guarantees at-least-once event delivery to RabbitMQ.
// Events are saved to this table in the same DB transaction as the entity change.
// A dedicated scheduler scans for PENDING events and publishes them to RabbitMQ.
// Once published, publishedAt is set and the event is never re-published.
// This prevents event loss if the service crashes between a DB write and a direct publish.
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Type of domain event — determines which RabbitMQ binding to use when publishing.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventType eventType;

    // JSON-serialized event payload — deserialized by the outbox scheduler before publishing.
    // Jackson ObjectMapper is used for serialization and deserialization.
    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    // Current processing status of this outbox event.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status;

    // Optional correlation id — links this outbox event to a Booking for traceability.
    // Nullable because some events (e.g. EVENT_NOT_FOUND) have no booking yet.
    private Long bookingId;

    // Optional correlation id — links this outbox event to a BookingAttempt for traceability.
    private Long bookingAttemptId;

    // Set by the outbox scheduler when the event is successfully published to RabbitMQ.
    // Null means the event has not been published yet.
    private LocalDateTime publishedAt;

    // Number of publish attempts — used to detect stuck or permanently failing events.
    // Events with retryCount > 3 should be investigated manually.
    @Column(nullable = false)
    @Builder.Default
    private int retryCount = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
