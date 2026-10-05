package com.securops.modules.shifts.service;

import com.securops.modules.guards.entity.Guard;
import com.securops.modules.guards.repository.GuardRepository;
import com.securops.modules.posts.entity.SecurityPost;
import com.securops.modules.posts.repository.SecurityPostRepository;
import com.securops.modules.shifts.engine.ShiftMeshGenerationRequest;
import com.securops.modules.shifts.engine.ShiftMeshGenerationResult;
import com.securops.modules.shifts.engine.ShiftMeshGeneratorEngine;
import com.securops.modules.shifts.entity.*;
import com.securops.modules.shifts.repository.RotationSchemeRepository;
import com.securops.modules.shifts.repository.ShiftScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftManagementService {

    private final ShiftMeshGeneratorEngine generatorEngine;
    private final ShiftScheduleRepository scheduleRepository;
    private final SecurityPostRepository postRepository;
    private final GuardRepository guardRepository;
    private final RotationSchemeRepository rotationSchemeRepository;
    private final com.securops.modules.attendance.repository.AttendanceRecordRepository attendanceRecordRepository;

    @Transactional
    public ShiftMeshGenerationResult generateMonthlyMesh(
            Long postId,
            Long rotationSchemeId,
            List<Long> guardIds,
            int year,
            int month,
            LocalDate anchorDate
    ) {
        SecurityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));

        RotationScheme scheme = rotationSchemeRepository.findById(rotationSchemeId)
                .orElseThrow(() -> new IllegalArgumentException("Rotation scheme not found with ID: " + rotationSchemeId));

        List<Guard> guards = guardRepository.findAllById(guardIds);
        if (guards.isEmpty()) {
            throw new IllegalArgumentException("No valid guards provided for assignment");
        }

        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = targetMonth.atDay(1);
        LocalDate endOfMonth = targetMonth.atEndOfMonth();

        // 1. Remove existing schedules for this post in the month
        List<ShiftSchedule> existingPostSchedules = scheduleRepository.findBySecurityPostIdAndShiftDateBetween(postId, startOfMonth, endOfMonth);
        if (!existingPostSchedules.isEmpty()) {
            log.info("Removing {} existing schedules for post {} in {}", existingPostSchedules.size(), post.getName(), targetMonth);
            for (ShiftSchedule s : existingPostSchedules) {
                attendanceRecordRepository.findByShiftScheduleId(s.getId()).ifPresent(attendanceRecordRepository::delete);
            }
            scheduleRepository.deleteAllInBatch(existingPostSchedules);
        }

        // 2. Remove any existing schedules for the assigned guards in this date range to prevent constraint collisions
        for (Guard g : guards) {
            List<ShiftSchedule> guardSchedules = scheduleRepository.findByGuardIdAndShiftDateBetween(g.getId(), startOfMonth, endOfMonth);
            if (!guardSchedules.isEmpty()) {
                for (ShiftSchedule s : guardSchedules) {
                    attendanceRecordRepository.findByShiftScheduleId(s.getId()).ifPresent(attendanceRecordRepository::delete);
                }
                scheduleRepository.deleteAllInBatch(guardSchedules);
            }
        }

        // Flush all pending deletes to database before generating and inserting new ones
        scheduleRepository.flush();

        ShiftMeshGenerationRequest request = ShiftMeshGenerationRequest.builder()
                .securityPost(post)
                .rotationScheme(scheme)
                .assignedGuards(guards)
                .targetMonth(targetMonth)
                .cycleAnchorDate(anchorDate != null ? anchorDate : startOfMonth)
                .build();

        ShiftMeshGenerationResult result = generatorEngine.generateMonthlyMesh(request);

        // Persist generated schedules
        List<ShiftSchedule> saved = scheduleRepository.saveAll(result.getSchedules());
        log.info("Persisted {} schedules for post {} in month {}", saved.size(), post.getName(), targetMonth);

        return result;
    }

    @Transactional
    public ShiftSchedule assignReliefGuard(Long scheduleId, Long reliefGuardId, String reason) {
        ShiftSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Shift schedule not found with ID: " + scheduleId));

        Guard reliefGuard = guardRepository.findById(reliefGuardId)
                .orElseThrow(() -> new IllegalArgumentException("Relief guard not found with ID: " + reliefGuardId));

        schedule.setReliefGuard(reliefGuard);
        schedule.setReplacementReason(reason);
        schedule.setStatus(ShiftScheduleStatus.REPLACED);

        log.info("Assigned relief guard {} to schedule {} on post {}",
                reliefGuard.getFullName(), schedule.getId(), schedule.getSecurityPost().getName());

        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public List<ShiftSchedule> getPostMonthlySchedule(Long postId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return scheduleRepository.findBySecurityPostIdAndShiftDateBetween(postId, ym.atDay(1), ym.atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public List<ShiftSchedule> getUncoveredShifts(LocalDate date) {
        return scheduleRepository.findByShiftDateAndStatus(date, ShiftScheduleStatus.UNCOVERED);
    }
}
