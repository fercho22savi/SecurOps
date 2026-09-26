package com.securops.modules.sync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "ops_sync_idempotency",
    indexes = {
        @Index(name = "idx_idem_event_id", columnList = "event_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true, nullable = false, length = 64)
    private String eventId;

    @Column(name = "origin_node_id", nullable = false, length = 50)
    private String originNodeId;

    @Column(name = "processed_at", nullable = false)
    @Builder.Default
    private LocalDateTime processedAt = LocalDateTime.now();

    @Column(name = "response_hash", length = 64)
    private String responseHash;
}
