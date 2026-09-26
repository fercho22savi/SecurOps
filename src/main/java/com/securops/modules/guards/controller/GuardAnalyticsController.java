package com.securops.modules.guards.controller;

import com.securops.modules.guards.dto.GuardDtos.*;
import com.securops.modules.guards.dto.GuardMetricsDto;
import com.securops.modules.guards.service.GuardScoringService;
import com.securops.modules.guards.service.GuardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/guards")
@RequiredArgsConstructor
public class GuardAnalyticsController {

    private final GuardScoringService guardScoringService;
    private final GuardService guardService;

    @GetMapping
    public ResponseEntity<List<GuardResponseDto>> getAllGuards() {
        return ResponseEntity.ok(guardService.getAllGuards());
    }

    @GetMapping("/available")
    public ResponseEntity<List<GuardResponseDto>> getAvailableGuards() {
        return ResponseEntity.ok(guardService.getAvailableGuards());
    }

    @PostMapping
    public ResponseEntity<GuardResponseDto> createGuard(@RequestBody CreateGuardRequestDto dto) {
        GuardResponseDto created = guardService.createGuard(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GuardResponseDto> deactivateGuard(
            @PathVariable Long id,
            @RequestBody(required = false) DeactivateGuardRequestDto dto) {
        String reason = dto != null ? dto.getReason() : "Retiro / Desvinculación de la empresa";
        return ResponseEntity.ok(guardService.deactivateGuard(id, reason));
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<GuardResponseDto> reactivateGuard(@PathVariable Long id) {
        return ResponseEntity.ok(guardService.reactivateGuard(id));
    }

    @GetMapping("/{guardId}/metrics")
    public ResponseEntity<GuardMetricsDto> getGuardMetrics(
            @PathVariable Long guardId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        GuardMetricsDto metrics = guardScoringService.calculateAndPersistScore(guardId, startDate, endDate);
        return ResponseEntity.ok(metrics);
    }
}
