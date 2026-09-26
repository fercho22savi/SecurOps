package com.securops.modules.guards.service;

import com.securops.modules.attendance.entity.AttendanceRecord;
import com.securops.modules.attendance.entity.AttendanceStatus;
import com.securops.modules.attendance.repository.AttendanceRecordRepository;
import com.securops.modules.guards.dto.GuardMetricsDto;
import com.securops.modules.guards.entity.Guard;
import com.securops.modules.guards.repository.GuardRepository;
import com.securops.modules.monitoring.entity.CallStatus;
import com.securops.modules.monitoring.repository.ControlCallLogRepository;
import com.securops.modules.monitoring.repository.NoveltyRecordRepository;
import com.securops.modules.shifts.entity.ShiftSchedule;
import com.securops.modules.shifts.entity.ShiftType;
import com.securops.modules.shifts.repository.ShiftScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuardScoringService {

    private final GuardRepository guardRepository;
    private final ShiftScheduleRepository scheduleRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final ControlCallLogRepository callLogRepository;
    private final NoveltyRecordRepository noveltyRepository;

    private static final double WEIGHT_ATTENDANCE = 0.40;
    private static final double WEIGHT_PUNCTUALITY = 0.25;
    private static final double WEIGHT_CALL_COMPLIANCE = 0.35;
    private static final double PENALTY_PER_DISCIPLINARY = 10.0;

    @Transactional
    public GuardMetricsDto calculateAndPersistScore(Long guardId, LocalDate startDate, LocalDate endDate) {
        Guard guard = guardRepository.findById(guardId)
                .orElseThrow(() -> new IllegalArgumentException("Guard not found with ID: " + guardId));

        // 1. Scheduled shifts vs Attendance
        List<ShiftSchedule> schedules = scheduleRepository.findByGuardIdAndShiftDateBetween(guardId, startDate, endDate);
        long totalWorkingShifts = schedules.stream()
                .filter(s -> s.getShiftDefinition().getType() != ShiftType.OFF_DUTY)
                .count();

        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt = endDate.atTime(23, 59, 59);
        List<AttendanceRecord> attendances = attendanceRepository.findByGuardAndDateRange(guardId, startDt, endDt);

        long attendedCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ON_TIME || a.getStatus() == AttendanceStatus.DELAYED)
                .count();

        long onTimeCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ON_TIME)
                .count();

        long delayCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.DELAYED)
                .count();

        double attendanceRate = totalWorkingShifts > 0 ? ((double) attendedCount / totalWorkingShifts) * 100.0 : 100.0;
        double punctualityRate = attendedCount > 0 ? ((double) onTimeCount / attendedCount) * 100.0 : 100.0;

        // 2. Control call round compliance
        long totalCalls = callLogRepository.countTotalByGuard(guardId);
        long onTimeCalls = callLogRepository.countByGuardAndStatus(guardId, CallStatus.ON_TIME);
        double callComplianceRate = totalCalls > 0 ? ((double) onTimeCalls / totalCalls) * 100.0 : 100.0;

        // 3. Disciplinary penalties
        long disciplinaryActions = noveltyRepository.countByGuardIdAndTriggersDisciplinaryActionTrue(guardId);

        // Weighted score calculation
        double rawScore = (attendanceRate * WEIGHT_ATTENDANCE)
                + (punctualityRate * WEIGHT_PUNCTUALITY)
                + (callComplianceRate * WEIGHT_CALL_COMPLIANCE)
                - (disciplinaryActions * PENALTY_PER_DISCIPLINARY);

        double finalScore = Math.max(0.0, Math.min(100.0, rawScore));
        BigDecimal finalScoreBd = BigDecimal.valueOf(finalScore).setScale(2, RoundingMode.HALF_UP);

        // Update guard entity
        guard.setPerformanceScore(finalScoreBd);
        guardRepository.save(guard);

        String tier;
        if (finalScore >= 90.0) tier = "EXCELLENT";
        else if (finalScore >= 75.0) tier = "GOOD";
        else if (finalScore >= 60.0) tier = "FAIR";
        else tier = "AT_RISK";

        log.info("Guard {} score calculated: {} (Tier: {})", guard.getFullName(), finalScoreBd, tier);

        return GuardMetricsDto.builder()
                .guardId(guardId)
                .guardName(guard.getFullName())
                .overallScore(finalScoreBd)
                .attendancePercentage(round(attendanceRate))
                .punctualityPercentage(round(punctualityRate))
                .callCompliancePercentage(round(callComplianceRate))
                .totalScheduledShifts(totalWorkingShifts)
                .delaysCount(delayCount)
                .disciplinaryActionsCount(disciplinaryActions)
                .performanceTier(tier)
                .build();
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
