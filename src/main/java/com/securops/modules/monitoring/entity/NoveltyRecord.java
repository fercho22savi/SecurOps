package com.securops.modules.monitoring.entity;

import com.securops.modules.guards.entity.Guard;
import com.securops.modules.posts.entity.SecurityPost;
import com.securops.modules.security.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "ops_novelty_records",
    indexes = {
        @Index(name = "idx_nov_guard_dates", columnList = "guard_id, start_date, end_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoveltyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guard_id", nullable = false)
    private Guard guard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_post_id")
    private SecurityPost securityPost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 35)
    private NoveltyType type;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean impactsPayroll = true;

    @Column(name = "payroll_discount_hours")
    @Builder.Default
    private Integer payrollDiscountHours = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean triggersDisciplinaryAction = false;

    @Column(length = 500, nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    @Column(name = "support_doc_url", length = 300)
    private String supportDocumentUrl;
}
