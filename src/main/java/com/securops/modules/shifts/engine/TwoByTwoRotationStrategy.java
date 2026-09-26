package com.securops.modules.shifts.engine;

import com.securops.modules.posts.entity.ServiceCoverageType;
import com.securops.modules.shifts.entity.RotationPattern;
import com.securops.modules.shifts.entity.ShiftType;
import org.springframework.stereotype.Component;

/**
 * 2x2x2 12-Hour Rotation Strategy:
 * Cycle duration: 6 days.
 * Guard base cycle: [DAY_12H, DAY_12H, NIGHT_12H, NIGHT_12H, OFF_DUTY, OFF_DUTY]
 * For a 24/7 post, 3 guards (or squads) are required with 2-day phase offsets:
 *   - Guard 0 (offset 0): D, D, N, N, L, L
 *   - Guard 1 (offset 2): L, L, D, D, N, N
 *   - Guard 2 (offset 4): N, N, L, L, D, D
 * Result: Every single day has exactly 1 Day guard, 1 Night guard, and 1 Off guard.
 */
@Component
public class TwoByTwoRotationStrategy implements RotationStrategy {

    private static final int CYCLE_LENGTH = 6;
    private static final ShiftType[] BASE_CYCLE = {
        ShiftType.DAY_12H,
        ShiftType.DAY_12H,
        ShiftType.NIGHT_12H,
        ShiftType.NIGHT_12H,
        ShiftType.OFF_DUTY,
        ShiftType.OFF_DUTY
    };

    @Override
    public RotationPattern getSupportedPattern() {
        return RotationPattern.ROTATION_2X2X2_12H;
    }

    @Override
    public int getCycleLengthDays() {
        return CYCLE_LENGTH;
    }

    @Override
    public int getRequiredGuards(ServiceCoverageType coverageType) {
        if (coverageType == ServiceCoverageType.CONTINUOUS_24_7) {
            return 3;
        } else if (coverageType == ServiceCoverageType.DAYTIME_12H || coverageType == ServiceCoverageType.NIGHTTIME_12H) {
            return 2;
        }
        return 3;
    }

    @Override
    public ShiftType calculateShiftForDay(int dayOfCycle, int guardSquadIndex, ServiceCoverageType coverageType) {
        if (coverageType == ServiceCoverageType.CONTINUOUS_24_7) {
            // Guard 0 has offset 0, Guard 1 has offset 2, Guard 2 has offset 4
            int phaseOffset = (guardSquadIndex * 2) % CYCLE_LENGTH;
            int effectiveDay = (dayOfCycle - phaseOffset + CYCLE_LENGTH) % CYCLE_LENGTH;
            return BASE_CYCLE[effectiveDay];
        } else if (coverageType == ServiceCoverageType.DAYTIME_12H) {
            // 2 Day, 2 Off, 2 Off for 12h day only (2 guards alternating)
            int effectiveDay = (dayOfCycle + (guardSquadIndex * 2)) % 4;
            return (effectiveDay < 2) ? ShiftType.DAY_12H : ShiftType.OFF_DUTY;
        } else if (coverageType == ServiceCoverageType.NIGHTTIME_12H) {
            int effectiveDay = (dayOfCycle + (guardSquadIndex * 2)) % 4;
            return (effectiveDay < 2) ? ShiftType.NIGHT_12H : ShiftType.OFF_DUTY;
        }

        // Default 24/7
        int phaseOffset = (guardSquadIndex * 2) % CYCLE_LENGTH;
        int effectiveDay = (dayOfCycle - phaseOffset + CYCLE_LENGTH) % CYCLE_LENGTH;
        return BASE_CYCLE[effectiveDay];
    }
}
