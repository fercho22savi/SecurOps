package com.securops.modules.guards.controller;

import com.securops.modules.guards.dto.GuardMetricsDto;
import com.securops.modules.guards.service.GuardScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/guards")
@RequiredArgsConstructor
public class GuardAnalyticsController {

    private final GuardScoringService guardScoringService;

    @GetMapping("/{guardId}/metrics")
    public ResponseEntity<GuardMetricsDto> getGuardMetrics(
            @PathVariable Long guardId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        GuardMetricsDto metrics = guardScoringService.calculateAndPersistScore(guardId, startDate, endDate);
        return ResponseEntity.ok(metrics);
    }
}
