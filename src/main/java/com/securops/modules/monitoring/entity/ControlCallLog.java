package com.securops.modules.monitoring.entity;

import com.securops.modules.guards.entity.Guard;
import com.securops.modules.posts.entity.SecurityPost;
import com.securops.modules.security.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(
    name = "ops_control_call_logs",
    indexes = {
        @Index(name = "idx_call_post_time", columnList = "security_post_id, scheduled_call_time"),
        @Index(name = "idx_call_guard", columnList = "guard_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ControlCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "security_post_id", nullable = false)
    private SecurityPost securityPost;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guard_id", nullable = false)
    private Guard guard;

    @Column(name = "scheduled_call_time", nullable = false)
    private LocalDateTime scheduledCallTime;

    @Column(name = "actual_call_time")
    private LocalDateTime actualCallTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private CallStatus status = CallStatus.ON_TIME;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_user_id")
    private User operator;

    @Column(length = 500)
    private String logMinuteNote; // Reporte de minuta o novedades durante la llamada

    @Column(name = "is_synced")
    @Builder.Default
    private Boolean isSynced = true;
}
