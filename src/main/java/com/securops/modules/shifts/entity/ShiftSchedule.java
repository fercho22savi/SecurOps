package com.securops.modules.shifts.entity;

import com.securops.modules.guards.entity.Guard;
import com.securops.modules.posts.entity.SecurityPost;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "ops_shift_schedules",
    indexes = {
        @Index(name = "idx_shift_post_date", columnList = "security_post_id, shift_date"),
        @Index(name = "idx_shift_guard_date", columnList = "guard_id, shift_date")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_guard_date_shift",
            columnNames = {"guard_id", "shift_date"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "security_post_id", nullable = false)
    private SecurityPost securityPost;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guard_id", nullable = false)
    private Guard guard;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shift_definition_id", nullable = false)
    private ShiftDefinition shiftDefinition;

    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private ShiftScheduleStatus status = ShiftScheduleStatus.SCHEDULED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relief_guard_id")
    private Guard reliefGuard; // Guarda de relevo si hubo novedad

    @Column(length = 255)
    private String replacementReason;

    @Column(length = 255)
    private String operationalNotes;

    @Version
    private Long version; // Optimistic locking for concurrency
}
