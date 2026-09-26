package com.securops.modules.attendance.entity;

import com.securops.modules.guards.entity.Guard;
import com.securops.modules.shifts.entity.ShiftSchedule;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "ops_attendance_records",
    indexes = {
        @Index(name = "idx_att_guard_time", columnList = "guard_id, check_in_time")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shift_schedule_id", nullable = false, unique = true)
    private ShiftSchedule shiftSchedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guard_id", nullable = false)
    private Guard guard;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "delay_minutes")
    @Builder.Default
    private Integer delayMinutes = 0;

    @Column(name = "actual_worked_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal actualWorkedHours = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.ON_TIME;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_method", length = 30)
    private VerificationMethod verificationMethod;

    @Column(name = "check_in_lat", precision = 10, scale = 7)
    private BigDecimal checkInLatitude;

    @Column(name = "check_in_lng", precision = 10, scale = 7)
    private BigDecimal checkInLongitude;

    @Column(name = "is_geofence_valid")
    @Builder.Default
    private Boolean isGeofenceValid = true;

    @Column(length = 255)
    private String supervisorNotes;
}
