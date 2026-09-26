package com.securops.modules.sync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "ops_sync_outbox",
    indexes = {
        @Index(name = "idx_outbox_status_created", columnList = "status, created_at"),
        @Index(name = "idx_outbox_event_id", columnList = "event_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncEventOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true, nullable = false, length = 64)
    private String eventId; // UUIDv4 idempotency key

    @Column(name = "origin_node_id", nullable = false, length = 50)
    private String originNodeId; // Sede / Nodo que originó el evento

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType; // ATTENDANCE, CONTROL_CALL, NOVELTY, SHIFT

    @Column(name = "aggregate_id", nullable = false, length = 50)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType; // CREATED, UPDATED, DELETED

    @Lob
    @Column(name = "payload_json", nullable = false)
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private SyncStatus status = SyncStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "error_message", length = 500)
    private String errorMessage;
}
