package com.securops.modules.shifts.engine;

import com.securops.modules.posts.entity.ServiceCoverageType;
import com.securops.modules.shifts.entity.RotationPattern;
import com.securops.modules.shifts.entity.ShiftType;
import org.springframework.stereotype.Component;

/**
 * 4x4 12-Hour Rotation Strategy:
 * Cycle duration: 8 days.
 * Guard base cycle: [D, D, N, N, OFF, OFF, OFF, OFF] (4 working days, 4 off days).
 * For 24/7 coverage: 4 guards with 2-day phase offsets.
 */
@Component
public class FourByFourRotationStrategy implements RotationStrategy {

    private static final int CYCLE_LENGTH = 8;
    private static final ShiftType[] BASE_CYCLE = {
        ShiftType.DAY_12H,
        ShiftType.DAY_12H,
        ShiftType.NIGHT_12H,
        ShiftType.NIGHT_12H,
        ShiftType.OFF_DUTY,
        ShiftType.OFF_DUTY,
        ShiftType.OFF_DUTY,
        ShiftType.OFF_DUTY
    };

    @Override
    public RotationPattern getSupportedPattern() {
        return RotationPattern.ROTATION_4X4_12H;
    }

    @Override
    public int getCycleLengthDays() {
        return CYCLE_LENGTH;
    }

    @Override
    public int getRequiredGuards(ServiceCoverageType coverageType) {
        if (coverageType == ServiceCoverageType.DAYTIME_12H || coverageType == ServiceCoverageType.NIGHTTIME_12H) {
            return 2;
        }
        return 4;
    }

    @Override
    public ShiftType calculateShiftForDay(int dayOfCycle, int guardSquadIndex, ServiceCoverageType coverageType) {
        if (coverageType == ServiceCoverageType.DAYTIME_12H) {
            int effectiveDay = (dayOfCycle + (guardSquadIndex * 4)) % 8;
            return (effectiveDay < 4) ? ShiftType.DAY_12H : ShiftType.OFF_DUTY;
        } else if (coverageType == ServiceCoverageType.NIGHTTIME_12H) {
            int effectiveDay = (dayOfCycle + (guardSquadIndex * 4)) % 8;
            return (effectiveDay < 4) ? ShiftType.NIGHT_12H : ShiftType.OFF_DUTY;
        }

        int phaseOffset = (guardSquadIndex * 2) % CYCLE_LENGTH;
        int effectiveDay = (dayOfCycle - phaseOffset + CYCLE_LENGTH) % CYCLE_LENGTH;
        return BASE_CYCLE[effectiveDay];
    }
}
