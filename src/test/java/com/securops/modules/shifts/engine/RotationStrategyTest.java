package com.securops.modules.shifts.engine;

import com.securops.modules.posts.entity.ServiceCoverageType;
import com.securops.modules.shifts.entity.ShiftType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RotationStrategyTest {

    private final TwoByTwoRotationStrategy twoByTwo = new TwoByTwoRotationStrategy();
    private final SixByOneRotationStrategy sixByOne = new SixByOneRotationStrategy();

    @Test
    @DisplayName("2x2x2 Strategy: 3 guards must guarantee exactly 1 Day, 1 Night, and 1 Off each day")
    void testTwoByTwoContinuity() {
        int cycleDays = twoByTwo.getCycleLengthDays();
        assertEquals(6, cycleDays);
        assertEquals(3, twoByTwo.getRequiredGuards(ServiceCoverageType.CONTINUOUS_24_7));

        for (int day = 0; day < cycleDays; day++) {
            Set<ShiftType> shiftsToday = new HashSet<>();
            for (int guard = 0; guard < 3; guard++) {
                ShiftType type = twoByTwo.calculateShiftForDay(day, guard, ServiceCoverageType.CONTINUOUS_24_7);
                shiftsToday.add(type);
            }

            assertTrue(shiftsToday.contains(ShiftType.DAY_12H), "Day " + day + " missing DAY_12H");
            assertTrue(shiftsToday.contains(ShiftType.NIGHT_12H), "Day " + day + " missing NIGHT_12H");
            assertTrue(shiftsToday.contains(ShiftType.OFF_DUTY), "Day " + day + " missing OFF_DUTY");
            assertEquals(3, shiftsToday.size(), "Day " + day + " has overlapping guard assignments");
        }
    }

    @Test
    @DisplayName("6x1 Strategy: 7-day cycle has exactly 6 work days and 1 rest day")
    void testSixByOneRestDays() {
        int cycleDays = sixByOne.getCycleLengthDays();
        assertEquals(7, cycleDays);

        int workDays = 0;
        int restDays = 0;

        for (int day = 0; day < cycleDays; day++) {
            ShiftType type = sixByOne.calculateShiftForDay(day, 0, ServiceCoverageType.CONTINUOUS_8H_3_SHIFTS);
            if (type == ShiftType.OFF_DUTY) {
                restDays++;
            } else {
                workDays++;
            }
        }

        assertEquals(6, workDays, "Should have 6 working shifts in a 7-day cycle");
        assertEquals(1, restDays, "Should have 1 rest day in a 7-day cycle");
    }
}
