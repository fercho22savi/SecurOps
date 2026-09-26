package com.securops.modules.monitoring.repository;

import com.securops.modules.monitoring.entity.NoveltyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoveltyRecordRepository extends JpaRepository<NoveltyRecord, Long> {

    @Query("SELECT n FROM NoveltyRecord n WHERE n.guard.id = :guardId " +
           "AND n.startDate <= :end AND n.endDate >= :start")
    List<NoveltyRecord> findNoveltiesForGuardInPeriod(
            @Param("guardId") Long guardId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    long countByGuardIdAndTriggersDisciplinaryActionTrue(Long guardId);
}
