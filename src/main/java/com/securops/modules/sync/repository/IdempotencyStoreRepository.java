package com.securops.modules.sync.repository;

import com.securops.modules.sync.entity.IdempotencyStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotencyStoreRepository extends JpaRepository<IdempotencyStore, Long> {
    Optional<IdempotencyStore> findByEventId(String eventId);
    boolean existsByEventId(String eventId);
}
