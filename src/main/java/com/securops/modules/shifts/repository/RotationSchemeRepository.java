package com.securops.modules.shifts.repository;

import com.securops.modules.shifts.entity.RotationPattern;
import com.securops.modules.shifts.entity.RotationScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RotationSchemeRepository extends JpaRepository<RotationScheme, Long> {
    Optional<RotationScheme> findByPattern(RotationPattern pattern);
}
