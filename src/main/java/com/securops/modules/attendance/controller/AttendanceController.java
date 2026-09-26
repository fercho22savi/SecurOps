package com.securops.modules.attendance.controller;

import com.securops.modules.attendance.entity.AttendanceRecord;
import com.securops.modules.attendance.entity.VerificationMethod;
import com.securops.modules.attendance.service.AttendanceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Data
    public static class CheckInRequestDto {
        private Long scheduleId;
        private Long guardId;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private VerificationMethod verificationMethod;
    }

    @Data
    public static class CheckOutRequestDto {
        private Long attendanceId;
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceRecord> checkIn(@RequestBody CheckInRequestDto request) {
        AttendanceRecord record = attendanceService.recordCheckIn(
                request.getScheduleId(),
                request.getGuardId(),
                request.getLatitude(),
                request.getLongitude(),
                request.getVerificationMethod()
        );
        return ResponseEntity.ok(record);
    }

    @PostMapping("/check-out")
    public ResponseEntity<AttendanceRecord> checkOut(@RequestBody CheckOutRequestDto request) {
        AttendanceRecord record = attendanceService.recordCheckOut(
                request.getAttendanceId(),
                request.getLatitude(),
                request.getLongitude()
        );
        return ResponseEntity.ok(record);
    }

    @GetMapping("/guard/{guardId}")
    public ResponseEntity<List<AttendanceRecord>> getGuardAttendances(
            @PathVariable Long guardId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(attendanceService.getGuardAttendances(guardId, start, end));
    }
}
