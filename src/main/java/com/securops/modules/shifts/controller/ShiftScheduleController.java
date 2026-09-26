package com.securops.modules.shifts.controller;

import com.securops.modules.shifts.engine.ShiftMeshGenerationResult;
import com.securops.modules.shifts.entity.ShiftSchedule;
import com.securops.modules.shifts.service.ShiftManagementService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftScheduleController {

    private final ShiftManagementService shiftService;

    @Data
    public static class GenerateMeshRequestDto {
        private Long postId;
        private Long rotationSchemeId;
        private List<Long> guardIds;
        private int year;
        private int month;
        private LocalDate cycleAnchorDate;
    }

    @Data
    public static class AssignReliefRequestDto {
        private Long scheduleId;
        private Long reliefGuardId;
        private String reason;
    }

    @PostMapping("/generate-mesh")
    public ResponseEntity<ShiftMeshGenerationResult> generateMonthlyMesh(@RequestBody GenerateMeshRequestDto request) {
        ShiftMeshGenerationResult result = shiftService.generateMonthlyMesh(
                request.getPostId(),
                request.getRotationSchemeId(),
                request.getGuardIds(),
                request.getYear(),
                request.getMonth(),
                request.getCycleAnchorDate()
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/assign-relief")
    public ResponseEntity<ShiftSchedule> assignRelief(@RequestBody AssignReliefRequestDto request) {
        ShiftSchedule updated = shiftService.assignReliefGuard(
                request.getScheduleId(),
                request.getReliefGuardId(),
                request.getReason()
        );
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<ShiftSchedule>> getPostMonthlySchedule(
            @PathVariable Long postId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(shiftService.getPostMonthlySchedule(postId, year, month));
    }

    @GetMapping("/uncovered")
    public ResponseEntity<List<ShiftSchedule>> getUncoveredShifts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(shiftService.getUncoveredShifts(date));
    }
}
