package com.securops.modules.payroll.service;

import com.securops.modules.attendance.entity.AttendanceRecord;
import com.securops.modules.attendance.entity.AttendanceStatus;
import com.securops.modules.attendance.repository.AttendanceRecordRepository;
import com.securops.modules.guards.entity.Guard;
import com.securops.modules.guards.repository.GuardRepository;
import com.securops.modules.monitoring.entity.NoveltyRecord;
import com.securops.modules.monitoring.repository.NoveltyRecordRepository;
import com.securops.modules.payroll.entity.PayrollPeriod;
import com.securops.modules.payroll.entity.PayrollSettlement;
import com.securops.modules.payroll.repository.PayrollPeriodRepository;
import com.securops.modules.payroll.repository.PayrollSettlementRepository;
import com.securops.modules.shifts.entity.ShiftSchedule;
import com.securops.modules.shifts.entity.ShiftType;
import com.securops.modules.shifts.repository.ShiftScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollCalculatorService {

    private final PayrollPeriodRepository periodRepository;
    private final PayrollSettlementRepository settlementRepository;
    private final ShiftScheduleRepository scheduleRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final NoveltyRecordRepository noveltyRepository;
    private final GuardRepository guardRepository;

    // Standard multipliers for security industry labor calculations
    private static final BigDecimal SURCHARGE_NIGHT_FACTOR = new BigDecimal("0.35");
    private static final BigDecimal OVERTIME_DAY_FACTOR = new BigDecimal("1.25");
    private static final BigDecimal OVERTIME_NIGHT_FACTOR = new BigDecimal("1.75");
    private static final BigDecimal SUNDAY_DAY_FACTOR = new BigDecimal("1.75");
    private static final BigDecimal SUNDAY_NIGHT_FACTOR = new BigDecimal("2.10");
    private static final BigDecimal SUNDAY_OVERTIME_DAY_FACTOR = new BigDecimal("2.00");
    private static final BigDecimal SUNDAY_OVERTIME_NIGHT_FACTOR = new BigDecimal("2.50");

    private static final BigDecimal STANDARD_MONTHLY_HOURS = new BigDecimal("230.00");
    private static final BigDecimal DEFAULT_BASE_SALARY = new BigDecimal("1600000.00"); // Reference base salary

    @Transactional
    public PayrollSettlement calculateSettlementForGuard(Long guardId, Long periodId) {
        Guard guard = guardRepository.findById(guardId)
                .orElseThrow(() -> new IllegalArgumentException("Guard not found with ID: " + guardId));
        PayrollPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Payroll period not found with ID: " + periodId));

        List<ShiftSchedule> schedules = scheduleRepository.findByGuardIdAndShiftDateBetween(
                guardId, period.getStartDate(), period.getEndDate());

        BigDecimal totalRegularDay = BigDecimal.ZERO;
        BigDecimal totalNightSurcharge = BigDecimal.ZERO;
        BigDecimal totalDayOvertime = BigDecimal.ZERO;
        BigDecimal totalNightOvertime = BigDecimal.ZERO;
        BigDecimal totalSundayDay = BigDecimal.ZERO;
        BigDecimal totalSundayNight = BigDecimal.ZERO;
        BigDecimal totalSundayDayOvertime = BigDecimal.ZERO;
        BigDecimal totalSundayNightOvertime = BigDecimal.ZERO;

        for (ShiftSchedule schedule : schedules) {
            ShiftType shiftType = schedule.getShiftDefinition().getType();
            if (shiftType == ShiftType.OFF_DUTY) {
                continue;
            }

            Optional<AttendanceRecord> attendanceOpt = attendanceRepository.findByShiftScheduleId(schedule.getId());
            boolean wasAbsent = attendanceOpt.map(a -> a.getStatus() == AttendanceStatus.ABSENT).orElse(false);
            if (wasAbsent) {
                continue; // Absence will be accounted for in deductions
            }

            LocalDate date = schedule.getShiftDate();
            boolean isSunday = date.getDayOfWeek() == DayOfWeek.SUNDAY;

            if (shiftType == ShiftType.DAY_12H) {
                if (isSunday) {
                    totalSundayDay = totalSundayDay.add(new BigDecimal("8.00"));
                    totalSundayDayOvertime = totalSundayDayOvertime.add(new BigDecimal("4.00"));
                } else {
                    totalRegularDay = totalRegularDay.add(new BigDecimal("8.00"));
                    totalDayOvertime = totalDayOvertime.add(new BigDecimal("4.00"));
                }
            } else if (shiftType == ShiftType.NIGHT_12H) {
                // 18:00 - 06:00: 3h day (18-21), 5h night regular (21-02), 4h night overtime (02-06)
                if (isSunday) {
                    totalSundayDay = totalSundayDay.add(new BigDecimal("3.00"));
                    totalSundayNight = totalSundayNight.add(new BigDecimal("5.00"));
                    totalSundayNightOvertime = totalSundayNightOvertime.add(new BigDecimal("4.00"));
                } else {
                    totalRegularDay = totalRegularDay.add(new BigDecimal("3.00"));
                    totalNightSurcharge = totalNightSurcharge.add(new BigDecimal("5.00"));
                    totalNightOvertime = totalNightOvertime.add(new BigDecimal("4.00"));
                }
            } else if (shiftType == ShiftType.MORNING_8H || shiftType == ShiftType.AFTERNOON_8H) {
                if (isSunday) {
                    totalSundayDay = totalSundayDay.add(new BigDecimal("8.00"));
                } else {
                    totalRegularDay = totalRegularDay.add(new BigDecimal("8.00"));
                }
            } else if (shiftType == ShiftType.NIGHT_8H) {
                // 22:00 - 06:00 (8h pure night)
                if (isSunday) {
                    totalSundayNight = totalSundayNight.add(new BigDecimal("8.00"));
                } else {
                    totalNightSurcharge = totalNightSurcharge.add(new BigDecimal("8.00"));
                }
            }
        }

        // Novelties calculation (deductions)
        LocalDateTime periodStart = period.getStartDate().atStartOfDay();
        LocalDateTime periodEnd = period.getEndDate().atTime(23, 59, 59);
        List<NoveltyRecord> novelties = noveltyRepository.findNoveltiesForGuardInPeriod(guardId, periodStart, periodEnd);

        BigDecimal totalDiscountHours = BigDecimal.ZERO;
        for (NoveltyRecord novelty : novelties) {
            if (novelty.isImpactsPayroll()) {
                totalDiscountHours = totalDiscountHours.add(new BigDecimal(novelty.getPayrollDiscountHours()));
            }
        }

        // Monetary calculation
        BigDecimal hourlyRate = DEFAULT_BASE_SALARY.divide(STANDARD_MONTHLY_HOURS, 4, RoundingMode.HALF_UP);

        BigDecimal surchargesAmount = BigDecimal.ZERO
                .add(totalNightSurcharge.multiply(hourlyRate).multiply(SURCHARGE_NIGHT_FACTOR))
                .add(totalDayOvertime.multiply(hourlyRate).multiply(OVERTIME_DAY_FACTOR))
                .add(totalNightOvertime.multiply(hourlyRate).multiply(OVERTIME_NIGHT_FACTOR))
                .add(totalSundayDay.multiply(hourlyRate).multiply(SUNDAY_DAY_FACTOR))
                .add(totalSundayNight.multiply(hourlyRate).multiply(SUNDAY_NIGHT_FACTOR))
                .add(totalSundayDayOvertime.multiply(hourlyRate).multiply(SUNDAY_OVERTIME_DAY_FACTOR))
                .add(totalSundayNightOvertime.multiply(hourlyRate).multiply(SUNDAY_OVERTIME_NIGHT_FACTOR));

        BigDecimal deductionsAmount = totalDiscountHours.multiply(hourlyRate);
        BigDecimal netPayable = DEFAULT_BASE_SALARY.add(surchargesAmount).subtract(deductionsAmount);

        PayrollSettlement settlement = settlementRepository.findByGuardIdAndPayrollPeriodId(guardId, periodId)
                .orElse(PayrollSettlement.builder()
                        .guard(guard)
                        .payrollPeriod(period)
                        .build());

        settlement.setRegularDayHours(totalRegularDay);
        settlement.setNightSurchargeHours(totalNightSurcharge);
        settlement.setDayOvertimeHours(totalDayOvertime);
        settlement.setNightOvertimeHours(totalNightOvertime);
        settlement.setSundayHolidayDayHours(totalSundayDay);
        settlement.setSundayHolidayNightHours(totalSundayNight);
        settlement.setSundayHolidayDayOvertimeHours(totalSundayDayOvertime);
        settlement.setSundayHolidayNightOvertimeHours(totalSundayNightOvertime);
        settlement.setDeductedAbsenceHours(totalDiscountHours);

        settlement.setBaseSalaryAmount(DEFAULT_BASE_SALARY);
        settlement.setSurchargesAndOvertimeAmount(surchargesAmount.setScale(2, RoundingMode.HALF_UP));
        settlement.setDeductionsAmount(deductionsAmount.setScale(2, RoundingMode.HALF_UP));
        settlement.setNetPayableAmount(netPayable.setScale(2, RoundingMode.HALF_UP));
        settlement.setCalculatedAt(LocalDateTime.now());

        log.info("Calculated payroll for guard {} (period {}): Net={}",
                guard.getFullName(), period.getPeriodCode(), settlement.getNetPayableAmount());

        return settlementRepository.save(settlement);
    }
}
