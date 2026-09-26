package com.securops.modules.monitoring.repository;

import com.securops.modules.monitoring.entity.CallStatus;
import com.securops.modules.monitoring.entity.ControlCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ControlCallLogRepository extends JpaRepository<ControlCallLog, Long> {

    List<ControlCallLog> findBySecurityPostIdAndScheduledCallTimeBetween(Long postId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(c) FROM ControlCallLog c WHERE c.guard.id = :guardId AND c.status = :status")
    long countByGuardAndStatus(@Param("guardId") Long guardId, @Param("status") CallStatus status);

    @Query("SELECT COUNT(c) FROM ControlCallLog c WHERE c.guard.id = :guardId")
    long countTotalByGuard(@Param("guardId") Long guardId);
}
