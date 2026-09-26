package com.securops.modules.sync.controller;

import com.securops.modules.sync.service.MultisiteSyncService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SyncController {

    private final MultisiteSyncService syncService;

    @Data
    public static class SyncBatchIngestRequest {
        private String originNodeId;
        private List<SyncEventDto> events;
    }

    @Data
    public static class SyncEventDto {
        private String eventId;
        private String aggregateType;
        private String payloadJson;
    }

    @PostMapping("/ingest")
    public ResponseEntity<String> ingestBatch(@RequestBody SyncBatchIngestRequest request) {
        int accepted = 0;
        for (SyncEventDto event : request.getEvents()) {
            boolean success = syncService.ingestEventAtCentral(
                    event.getEventId(),
                    request.getOriginNodeId(),
                    event.getAggregateType(),
                    event.getPayloadJson()
            );
            if (success) accepted++;
        }
        return ResponseEntity.ok(String.format("Accepted %d of %d events", accepted, request.getEvents().size()));
    }
}
