package com.tbs.paymentservice.entity;

import com.tbs.paymentservice.entity.enums.OutboxEventStatus;
import com.tbs.paymentservice.entity.enums.OutboxEventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_event")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
// Outbox pattern — guarantees at-least-once delivery to RabbitMQ.
// Saved in the same DB transaction as the Payment status change.
// OutboxScheduler publishes PENDING events and marks them PUBLISHED.
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventType eventType;

    // JSON-serialized event payload — deserialized by OutboxScheduler before publishing.
    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status;

    // Correlates this outbox event back to the originating payment record.
    private Long paymentId;

    // Set by OutboxScheduler when successfully published to RabbitMQ.
    private LocalDateTime publishedAt;

    // Incremented on each failed publish attempt; events above MAX_RETRY are skipped.
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
