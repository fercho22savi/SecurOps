package com.securops.modules.attendance.repository;

import com.securops.modules.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findByShiftScheduleId(Long shiftScheduleId);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.guard.id = :guardId AND a.checkInTime BETWEEN :start AND :end")
    List<AttendanceRecord> findByGuardAndDateRange(
        @Param("guardId") Long guardId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.guard.id = :guardId AND a.status = 'DELAYED'")
    long countDelaysByGuard(@Param("guardId") Long guardId);
}
