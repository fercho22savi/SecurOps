package com.securops.modules.shifts.repository;

import com.securops.modules.shifts.entity.ShiftDefinition;
import com.securops.modules.shifts.entity.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShiftDefinitionRepository extends JpaRepository<ShiftDefinition, Long> {
    Optional<ShiftDefinition> findByType(ShiftType type);
    Optional<ShiftDefinition> findByCode(String code);
}
