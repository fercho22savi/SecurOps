package com.securops.modules.monitoring.controller;

import com.securops.modules.monitoring.dto.MonitoringDtos.*;
import com.securops.modules.monitoring.entity.ControlCallLog;
import com.securops.modules.monitoring.entity.NoveltyRecord;
import com.securops.modules.monitoring.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    @PostMapping("/calls/log")
    public ResponseEntity<ControlCallLog> logControlCall(@RequestBody ControlCallRequestDto dto) {
        return ResponseEntity.ok(monitoringService.recordControlCall(dto));
    }

    @PostMapping("/novelties")
    public ResponseEntity<NoveltyRecord> reportNovelty(@RequestBody NoveltyReportRequestDto dto) {
        return ResponseEntity.ok(monitoringService.reportNovelty(dto));
    }

    @GetMapping("/calls/post/{postId}")
    public ResponseEntity<List<ControlCallLog>> getPostCalls(
            @PathVariable Long postId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(monitoringService.getPostCalls(postId, start, end));
    }

    @GetMapping("/novelties/guard/{guardId}")
    public ResponseEntity<List<NoveltyRecord>> getGuardNovelties(
            @PathVariable Long guardId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(monitoringService.getGuardNovelties(guardId, start, end));
    }
}
