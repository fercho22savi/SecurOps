package com.securops.modules.shifts.engine;

import com.securops.modules.posts.entity.ServiceCoverageType;
import com.securops.modules.shifts.entity.RotationPattern;
import com.securops.modules.shifts.entity.ShiftType;
import org.springframework.stereotype.Component;

/**
 * 3x3 12-Hour Rotation Strategy:
 * For 24/7 posts: 9-day cycle:
 * [D, D, D, N, N, N, OFF, OFF, OFF]
 * Requires 3 guards with 3-day phase offsets (0, 3, 6).
 * Result: 1 Day, 1 Night, 1 Off every day.
 */
@Component
public class ThreeByThreeRotationStrategy implements RotationStrategy {

    private static final int CYCLE_LENGTH = 9;
    private static final ShiftType[] BASE_CYCLE = {
        ShiftType.DAY_12H, ShiftType.DAY_12H, ShiftType.DAY_12H,
        ShiftType.NIGHT_12H, ShiftType.NIGHT_12H, ShiftType.NIGHT_12H,
        ShiftType.OFF_DUTY, ShiftType.OFF_DUTY, ShiftType.OFF_DUTY
    };

    @Override
    public RotationPattern getSupportedPattern() {
        return RotationPattern.ROTATION_3X3_12H;
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
        return 3;
    }

    @Override
    public ShiftType calculateShiftForDay(int dayOfCycle, int guardSquadIndex, ServiceCoverageType coverageType) {
        if (coverageType == ServiceCoverageType.DAYTIME_12H) {
            // 6-day cycle: 3 Days, 3 Off
            int effectiveDay = (dayOfCycle + (guardSquadIndex * 3)) % 6;
            return (effectiveDay < 3) ? ShiftType.DAY_12H : ShiftType.OFF_DUTY;
        } else if (coverageType == ServiceCoverageType.NIGHTTIME_12H) {
            int effectiveDay = (dayOfCycle + (guardSquadIndex * 3)) % 6;
            return (effectiveDay < 3) ? ShiftType.NIGHT_12H : ShiftType.OFF_DUTY;
        }

        // 24/7 coverage: 3 guards with 3-day offsets
        int phaseOffset = (guardSquadIndex * 3) % CYCLE_LENGTH;
        int effectiveDay = (dayOfCycle - phaseOffset + CYCLE_LENGTH) % CYCLE_LENGTH;
        return BASE_CYCLE[effectiveDay];
    }
}
