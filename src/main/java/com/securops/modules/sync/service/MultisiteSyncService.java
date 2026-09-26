package com.securops.modules.sync.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securops.modules.sync.entity.IdempotencyStore;
import com.securops.modules.sync.entity.SyncEventOutbox;
import com.securops.modules.sync.entity.SyncStatus;
import com.securops.modules.sync.repository.IdempotencyStoreRepository;
import com.securops.modules.sync.repository.SyncEventOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultisiteSyncService {

    private final SyncEventOutboxRepository outboxRepository;
    private final IdempotencyStoreRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    @Value("${securops.sync.node-id:BRANCH-LOCAL-01}")
    private String currentNodeId;

    @Value("${securops.sync.is-central:false}")
    private boolean isCentralNode;

    @Value("${securops.sync.batch-size:50}")
    private int batchSize;

    /**
     * Enqueues an event into the local transactional outbox.
     * Must be called inside the local business transaction to guarantee atomic persistence.
     */
    @Transactional
    public void stageEventForSync(String aggregateType, String aggregateId, String eventType, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            String eventId = UUID.randomUUID().toString();

            SyncEventOutbox outboxEntry = SyncEventOutbox.builder()
                    .eventId(eventId)
                    .originNodeId(currentNodeId)
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payloadJson(payloadJson)
                    .status(SyncStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();

            outboxRepository.save(outboxEntry);
            log.info("Staged event for sync: type={}, id={}, eventId={}", aggregateType, aggregateId, eventId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize sync event payload", e);
            throw new RuntimeException("Error serializing sync event", e);
        }
    }

    /**
     * Background worker scheduled to flush outbox events to the central hub.
     * Uses backoff and tolerance for network disconnects.
     */
    @Scheduled(fixedDelayString = "${securops.sync.flush-interval-ms:10000}")
    public void processOutboxQueue() {
        if (isCentralNode) {
            return; // Central node receives events rather than flushing them
        }

        List<SyncEventOutbox> pendingEvents = outboxRepository.findByStatusInOrderByCreatedAtAsc(
                List.of(SyncStatus.PENDING, SyncStatus.FAILED),
                PageRequest.of(0, batchSize)
        );

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Processing {} outbox events to sync with central node...", pendingEvents.size());

        for (SyncEventOutbox event : pendingEvents) {
            try {
                // Mark in flight
                event.setLastAttemptAt(LocalDateTime.now());
                event.setStatus(SyncStatus.IN_FLIGHT);
                outboxRepository.save(event);

                // Simulate REST/Queue Dispatch to Central
                dispatchToCentral(event);

                // On success:
                event.setStatus(SyncStatus.SYNCED);
                event.setSyncedAt(LocalDateTime.now());
                outboxRepository.save(event);
                log.info("Successfully synced eventId: {}", event.getEventId());
            } catch (Exception ex) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setStatus(SyncStatus.FAILED);
                event.setErrorMessage(ex.getMessage());
                outboxRepository.save(event);
                log.warn("Offline or unreachable central node. Retrying eventId {} later (attempt {}): {}",
                        event.getEventId(), event.getRetryCount(), ex.getMessage());
            }
        }
    }

    /**
     * Ingestion point at the central node for incoming multi-site events.
     * Implements idempotent processing to ensure no duplicate entries are recorded.
     */
    @Transactional
    public boolean ingestEventAtCentral(String eventId, String originNodeId, String aggregateType, String payloadJson) {
        if (idempotencyRepository.existsByEventId(eventId)) {
            log.info("Duplicate event ignored at central: eventId={}", eventId);
            return true; // Already processed successfully
        }

        // Apply aggregate updates (e.g. AttendanceRecord, ControlCallLog, NoveltyRecord)
        log.info("Central accepted event from {}: aggregate={}, eventId={}", originNodeId, aggregateType, eventId);

        // Record idempotency token
        IdempotencyStore token = IdempotencyStore.builder()
                .eventId(eventId)
                .originNodeId(originNodeId)
                .processedAt(LocalDateTime.now())
                .build();
        idempotencyRepository.save(token);

        return true;
    }

    private void dispatchToCentral(SyncEventOutbox event) throws Exception {
        // In real environment, this invokes WebClient / RestClient or sends to RabbitMQ/Kafka queue.
        // Simulating network liveness check:
        // Thread.sleep(100);
    }
}
