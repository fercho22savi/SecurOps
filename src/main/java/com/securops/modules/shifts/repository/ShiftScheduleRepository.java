package com.securops.modules.shifts.repository;

import com.securops.modules.shifts.entity.ShiftSchedule;
import com.securops.modules.shifts.entity.ShiftScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, Long> {

    List<ShiftSchedule> findBySecurityPostIdAndShiftDateBetween(Long postId, LocalDate start, LocalDate end);

    List<ShiftSchedule> findByGuardIdAndShiftDateBetween(Long guardId, LocalDate start, LocalDate end);

    List<ShiftSchedule> findByShiftDateAndStatus(LocalDate date, ShiftScheduleStatus status);

    @Query("SELECT s FROM ShiftSchedule s WHERE s.securityPost.id = :postId AND s.shiftDate = :date")
    List<ShiftSchedule> findByPostAndDate(@Param("postId") Long postId, @Param("date") LocalDate date);

    boolean existsByGuardIdAndShiftDate(Long guardId, LocalDate date);
}
