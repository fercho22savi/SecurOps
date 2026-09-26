package com.securops.modules.sync.repository;

import com.securops.modules.sync.entity.SyncEventOutbox;
import com.securops.modules.sync.entity.SyncStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SyncEventOutboxRepository extends JpaRepository<SyncEventOutbox, Long> {

    List<SyncEventOutbox> findByStatusInOrderByCreatedAtAsc(List<SyncStatus> statuses, Pageable pageable);

    Optional<SyncEventOutbox> findByEventId(String eventId);
}
