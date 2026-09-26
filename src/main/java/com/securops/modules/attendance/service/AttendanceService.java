package com.securops.modules.attendance.service;

import com.securops.modules.attendance.entity.AttendanceRecord;
import com.securops.modules.attendance.entity.AttendanceStatus;
import com.securops.modules.attendance.entity.VerificationMethod;
import com.securops.modules.attendance.repository.AttendanceRecordRepository;
import com.securops.modules.guards.entity.Guard;
import com.securops.modules.guards.repository.GuardRepository;
import com.securops.modules.posts.entity.SecurityPost;
import com.securops.modules.shifts.entity.ShiftSchedule;
import com.securops.modules.shifts.entity.ShiftScheduleStatus;
import com.securops.modules.shifts.repository.ShiftScheduleRepository;
import com.securops.modules.sync.service.MultisiteSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRepository;
    private final ShiftScheduleRepository scheduleRepository;
    private final GuardRepository guardRepository;
    private final MultisiteSyncService syncService;

    private static final int TOLERANCE_MINUTES = 15;
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    @Transactional
    public AttendanceRecord recordCheckIn(Long scheduleId, Long guardId, BigDecimal lat, BigDecimal lng, VerificationMethod method) {
        ShiftSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with ID: " + scheduleId));

        Guard guard = guardRepository.findById(guardId)
                .orElseThrow(() -> new IllegalArgumentException("Guard not found with ID: " + guardId));

        // Check if attendance already recorded
        if (attendanceRepository.findByShiftScheduleId(scheduleId).isPresent()) {
            throw new IllegalStateException("Check-in already registered for this shift schedule");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledStart = schedule.getShiftDate().atTime(schedule.getShiftDefinition().getStartTime());

        long minutesLate = 0;
        if (now.isAfter(scheduledStart)) {
            minutesLate = Duration.between(scheduledStart, now).toMinutes();
        }

        AttendanceStatus status = (minutesLate <= TOLERANCE_MINUTES) ? AttendanceStatus.ON_TIME : AttendanceStatus.DELAYED;

        // Geofence validation using Haversine formula
        boolean isGeofenceValid = true;
        SecurityPost post = schedule.getSecurityPost();
        if (lat != null && lng != null && post.getLatitude() != null && post.getLongitude() != null) {
            double distanceMeters = calculateHaversineDistance(
                    lat.doubleValue(), lng.doubleValue(),
                    post.getLatitude().doubleValue(), post.getLongitude().doubleValue()
            );
            int allowedRadius = post.getGeofenceRadiusMeters() != null ? post.getGeofenceRadiusMeters() : 50;
            isGeofenceValid = distanceMeters <= allowedRadius;

            if (!isGeofenceValid) {
                log.warn("GEOFENCE ALERT: Guard {} clocked in at {:.1f}m from post '{}' (allowed: {}m)",
                        guard.getFullName(), distanceMeters, post.getName(), allowedRadius);
            }
        }

        AttendanceRecord record = AttendanceRecord.builder()
                .shiftSchedule(schedule)
                .guard(guard)
                .checkInTime(now)
                .delayMinutes((int) minutesLate)
                .status(status)
                .verificationMethod(method != null ? method : VerificationMethod.MOBILE_APP_GPS)
                .checkInLatitude(lat)
                .checkInLongitude(lng)
                .isGeofenceValid(isGeofenceValid)
                .supervisorNotes(isGeofenceValid ? null : "ALERTA: Marcación fuera de geocerca permitida")
                .build();

        AttendanceRecord saved = attendanceRepository.save(record);

        schedule.setStatus(ShiftScheduleStatus.IN_PROGRESS);
        scheduleRepository.save(schedule);

        // Stage into outbox for multisite sync
        syncService.stageEventForSync("ATTENDANCE", saved.getId().toString(), "CHECK_IN", saved);

        log.info("Check-in recorded for guard {} on post {}. Delay: {}m, GeofenceValid: {}",
                guard.getFullName(), post.getName(), minutesLate, isGeofenceValid);

        return saved;
    }

    @Transactional
    public AttendanceRecord recordCheckOut(Long attendanceId, BigDecimal lat, BigDecimal lng) {
        AttendanceRecord record = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found with ID: " + attendanceId));

        LocalDateTime now = LocalDateTime.now();
        record.setCheckOutTime(now);

        if (record.getCheckInTime() != null) {
            long durationMinutes = Duration.between(record.getCheckInTime(), now).toMinutes();
            BigDecimal workedHours = BigDecimal.valueOf(durationMinutes)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            record.setActualWorkedHours(workedHours);
        }

        ShiftSchedule schedule = record.getShiftSchedule();
        schedule.setStatus(ShiftScheduleStatus.COMPLETED);
        scheduleRepository.save(schedule);

        AttendanceRecord updated = attendanceRepository.save(record);

        // Stage into outbox for multisite sync
        syncService.stageEventForSync("ATTENDANCE", updated.getId().toString(), "CHECK_OUT", updated);

        log.info("Check-out recorded for attendance ID {}. Worked hours: {}",
                record.getId(), record.getActualWorkedHours());

        return updated;
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecord> getGuardAttendances(Long guardId, LocalDateTime start, LocalDateTime end) {
        return attendanceRepository.findByGuardAndDateRange(guardId, start, end);
    }

    /**
     * Calculates distance in meters between two GPS coordinates using the Haversine formula.
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}
