package com.securops.modules.guards.repository;

import com.securops.modules.guards.entity.Guard;
import com.securops.modules.guards.entity.GuardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuardRepository extends JpaRepository<Guard, Long> {
    Optional<Guard> findByNationalId(String nationalId);
    List<Guard> findByStatus(GuardStatus status);
}
