package com.securops.modules.payroll.entity;

import com.securops.modules.guards.entity.Guard;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "ops_payroll_settlements",
    indexes = {
        @Index(name = "idx_pay_guard_period", columnList = "guard_id, payroll_period_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_guard_period", columnNames = {"guard_id", "payroll_period_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guard_id", nullable = false)
    private Guard guard;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payroll_period_id", nullable = false)
    private PayrollPeriod payrollPeriod;

    // Horas calculadas
    @Column(precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal regularDayHours = BigDecimal.ZERO;

    @Column(precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal nightSurchargeHours = BigDecimal.ZERO;

    @Column(precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal dayOvertimeHours = BigDecimal.ZERO;

    @Column(precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal nightOvertimeHours = BigDecimal.ZERO;

    @Column(precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal sundayHolidayDayHours = BigDecimal.ZERO;

    @Column(precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal sundayHolidayNightHours = BigDecimal.ZERO;

    @Column(precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal sundayHolidayDayOvertimeHours = BigDecimal.ZERO;

    @Column(precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal sundayHolidayNightOvertimeHours = BigDecimal.ZERO;

    @Column(precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal deductedAbsenceHours = BigDecimal.ZERO;

    // Totales monetarios calculados
    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal baseSalaryAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal surchargesAndOvertimeAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal deductionsAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal netPayableAmount = BigDecimal.ZERO;

    @Column(name = "calculated_at")
    @Builder.Default
    private LocalDateTime calculatedAt = LocalDateTime.now();
}
