package com.securops.modules.shifts.engine;

import com.securops.modules.posts.entity.ServiceCoverageType;
import com.securops.modules.shifts.entity.RotationPattern;
import com.securops.modules.shifts.entity.ShiftType;

/**
 * Strategy interface for dynamically generating shift rotations for guards and posts.
 */
public interface RotationStrategy {

    /**
     * The rotation pattern supported by this strategy.
     */
    RotationPattern getSupportedPattern();

    /**
     * Duration of the repeating cycle in days.
     */
    int getCycleLengthDays();

    /**
     * Minimum number of guards required to achieve 100% uninterrupted coverage
     * based on post service coverage type.
     */
    int getRequiredGuards(ServiceCoverageType coverageType);

    /**
     * Determines what shift a guard is assigned on a given relative day of the cycle.
     *
     * @param dayOfCycle relative day index in [0, cycleLengthDays - 1]
     * @param guardSquadIndex index of the guard within the rotation squad [0, squadSize - 1]
     * @param coverageType post coverage requirement (e.g. 24/7 vs 12h day only)
     * @return ShiftType assigned to the guard (e.g., DAY_12H, NIGHT_12H, OFF_DUTY)
     */
    ShiftType calculateShiftForDay(int dayOfCycle, int guardSquadIndex, ServiceCoverageType coverageType);
}
