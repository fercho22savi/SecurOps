package com.securops.modules.shifts.engine;

import com.securops.modules.posts.entity.ServiceCoverageType;
import com.securops.modules.shifts.entity.RotationPattern;
import com.securops.modules.shifts.entity.ShiftType;
import org.springframework.stereotype.Component;

/**
 * 5x2 8-Hour Rotation Strategy:
 * Cycle duration: 7 days.
 * 5 days work (Mon-Fri), 2 days off (Sat-Sun or designated 2 consecutive days).
 */
@Component
public class FiveByTwoRotationStrategy implements RotationStrategy {

    private static final int CYCLE_LENGTH = 7;

    @Override
    public RotationPattern getSupportedPattern() {
        return RotationPattern.ROTATION_5X2_8H;
    }

    @Override
    public int getCycleLengthDays() {
        return CYCLE_LENGTH;
    }

    @Override
    public int getRequiredGuards(ServiceCoverageType coverageType) {
        return 1;
    }

    @Override
    public ShiftType calculateShiftForDay(int dayOfCycle, int guardSquadIndex, ServiceCoverageType coverageType) {
        // dayOfCycle: 0 = Mon, 1 = Tue, 2 = Wed, 3 = Thu, 4 = Fri, 5 = Sat, 6 = Sun
        if (dayOfCycle >= 5) {
            return ShiftType.OFF_DUTY;
        }
        return ShiftType.MORNING_8H;
    }
}
