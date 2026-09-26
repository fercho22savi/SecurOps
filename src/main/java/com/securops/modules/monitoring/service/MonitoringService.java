package com.securops.modules.monitoring.service;

import com.securops.modules.guards.entity.Guard;
import com.securops.modules.guards.repository.GuardRepository;
import com.securops.modules.monitoring.dto.MonitoringDtos.*;
import com.securops.modules.monitoring.entity.CallStatus;
import com.securops.modules.monitoring.entity.ControlCallLog;
import com.securops.modules.monitoring.entity.NoveltyRecord;
import com.securops.modules.monitoring.entity.NoveltyType;
import com.securops.modules.monitoring.repository.ControlCallLogRepository;
import com.securops.modules.monitoring.repository.NoveltyRecordRepository;
import com.securops.modules.posts.entity.SecurityPost;
import com.securops.modules.posts.repository.SecurityPostRepository;
import com.securops.modules.security.entity.User;
import com.securops.modules.security.repository.UserRepository;
import com.securops.modules.shifts.entity.ShiftSchedule;
import com.securops.modules.shifts.entity.ShiftScheduleStatus;
import com.securops.modules.shifts.repository.ShiftScheduleRepository;
import com.securops.modules.sync.service.MultisiteSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final ControlCallLogRepository callLogRepository;
    private final NoveltyRecordRepository noveltyRepository;
    private final SecurityPostRepository postRepository;
    private final GuardRepository guardRepository;
    private final UserRepository userRepository;
    private final ShiftScheduleRepository scheduleRepository;
    private final MultisiteSyncService syncService;

    @Transactional
    public ControlCallLog recordControlCall(ControlCallRequestDto dto) {
        SecurityPost post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + dto.getPostId()));

        Guard guard = guardRepository.findById(dto.getGuardId())
                .orElseThrow(() -> new IllegalArgumentException("Guard not found with ID: " + dto.getGuardId()));

        User operator = null;
        if (dto.getOperatorUserId() != null) {
            operator = userRepository.findById(dto.getOperatorUserId()).orElse(null);
        }

        ControlCallLog logEntry = ControlCallLog.builder()
                .securityPost(post)
                .guard(guard)
                .scheduledCallTime(dto.getScheduledCallTime() != null ? dto.getScheduledCallTime() : LocalDateTime.now())
                .actualCallTime(dto.getActualCallTime() != null ? dto.getActualCallTime() : LocalDateTime.now())
                .status(dto.getStatus() != null ? dto.getStatus() : CallStatus.ON_TIME)
                .operator(operator)
                .logMinuteNote(dto.getLogMinuteNote())
                .isSynced(true)
                .build();

        ControlCallLog saved = callLogRepository.save(logEntry);

        // Stage for multisite synchronization
        syncService.stageEventForSync("CONTROL_CALL", saved.getId().toString(), "RECORDED", saved);

        log.info("Control call logged for post '{}', guard '{}', status: {}",
                post.getName(), guard.getFullName(), saved.getStatus());

        return saved;
    }

    @Transactional
    public NoveltyRecord reportNovelty(NoveltyReportRequestDto dto) {
        Guard guard = guardRepository.findById(dto.getGuardId())
                .orElseThrow(() -> new IllegalArgumentException("Guard not found with ID: " + dto.getGuardId()));

        SecurityPost post = null;
        if (dto.getPostId() != null) {
            post = postRepository.findById(dto.getPostId()).orElse(null);
        }

        User approvedBy = null;
        if (dto.getApprovedByUserId() != null) {
            approvedBy = userRepository.findById(dto.getApprovedByUserId()).orElse(null);
        }

        NoveltyRecord novelty = NoveltyRecord.builder()
                .guard(guard)
                .securityPost(post)
                .type(dto.getType())
                .startDate(dto.getStartDate() != null ? dto.getStartDate() : LocalDateTime.now())
                .endDate(dto.getEndDate() != null ? dto.getEndDate() : LocalDateTime.now().plusDays(1))
                .impactsPayroll(dto.isImpactsPayroll())
                .payrollDiscountHours(dto.getPayrollDiscountHours())
                .triggersDisciplinaryAction(dto.isTriggersDisciplinaryAction())
                .description(dto.getDescription())
                .approvedBy(approvedBy)
                .supportDocumentUrl(dto.getSupportDocUrl())
                .build();

        NoveltyRecord saved = noveltyRepository.save(novelty);

        // If the novelty incapacitates the guard, flag affected scheduled shifts as UNCOVERED
        if (dto.getType() == NoveltyType.MEDICAL_LEAVE ||
            dto.getType() == NoveltyType.UNJUSTIFIED_ABSENCE ||
            dto.getType() == NoveltyType.EMERGENCY_RELIEF ||
            dto.getType() == NoveltyType.SUSPENSION_DISCIPLINARY) {

            LocalDate start = novelty.getStartDate().toLocalDate();
            LocalDate end = novelty.getEndDate().toLocalDate();

            List<ShiftSchedule> affectedSchedules = scheduleRepository.findByGuardIdAndShiftDateBetween(
                    guard.getId(), start, end);

            for (ShiftSchedule s : affectedSchedules) {
                if (s.getStatus() == ShiftScheduleStatus.SCHEDULED || s.getStatus() == ShiftScheduleStatus.CONFIRMED) {
                    s.setStatus(ShiftScheduleStatus.UNCOVERED);
                    s.setReplacementReason("NOVEDAD: " + dto.getType() + " - " + dto.getDescription());
                    scheduleRepository.save(s);
                    log.warn("Shift schedule {} on post '{}' marked as UNCOVERED due to novelty",
                            s.getId(), s.getSecurityPost().getName());
                }
            }
        }

        // Stage for multisite synchronization
        syncService.stageEventForSync("NOVELTY", saved.getId().toString(), "REPORTED", saved);

        log.info("Novelty reported: type={}, guard={}, triggersDisciplinary={}",
                saved.getType(), guard.getFullName(), saved.isTriggersDisciplinaryAction());

        return saved;
    }

    @Transactional(readOnly = true)
    public List<ControlCallLog> getPostCalls(Long postId, LocalDateTime start, LocalDateTime end) {
        return callLogRepository.findBySecurityPostIdAndScheduledCallTimeBetween(postId, start, end);
    }

    @Transactional(readOnly = true)
    public List<NoveltyRecord> getGuardNovelties(Long guardId, LocalDateTime start, LocalDateTime end) {
        return noveltyRepository.findNoveltiesForGuardInPeriod(guardId, start, end);
    }
}
