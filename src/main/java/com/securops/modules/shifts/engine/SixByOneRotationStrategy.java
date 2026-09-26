package com.securops.modules.shifts.engine;

import com.securops.modules.posts.entity.ServiceCoverageType;
import com.securops.modules.shifts.entity.RotationPattern;
import com.securops.modules.shifts.entity.ShiftType;
import org.springframework.stereotype.Component;

/**
 * 6x1 8-Hour Rotation Strategy:
 * Cycle duration: 7 days.
 * 6 days of work, 1 day off duty.
 * For 24-hour posts with 3 shifts (Morning, Afternoon, Night):
 * 4 guards rotate with staggered rest days to cover 21 shifts per week.
 */
@Component
public class SixByOneRotationStrategy implements RotationStrategy {

    private static final int CYCLE_LENGTH = 7;

    @Override
    public RotationPattern getSupportedPattern() {
        return RotationPattern.ROTATION_6X1_8H;
    }

    @Override
    public int getCycleLengthDays() {
        return CYCLE_LENGTH;
    }

    @Override
    public int getRequiredGuards(ServiceCoverageType coverageType) {
        if (coverageType == ServiceCoverageType.CONTINUOUS_8H_3_SHIFTS) {
            return 4; // 3 on duty + 1 covering rest days
        }
        return 1;
    }

    @Override
    public ShiftType calculateShiftForDay(int dayOfCycle, int guardSquadIndex, ServiceCoverageType coverageType) {
        // In a 7-day cycle, the guard rests on their designated offset day
        int restDay = (guardSquadIndex * 2) % CYCLE_LENGTH;
        if (dayOfCycle == restDay) {
            return ShiftType.OFF_DUTY;
        }

        if (coverageType == ServiceCoverageType.CONTINUOUS_8H_3_SHIFTS) {
            // Rotating across Morning (06-14), Afternoon (14-22), Night (22-06)
            int shiftSlot = (dayOfCycle + guardSquadIndex) % 3;
            return switch (shiftSlot) {
                case 0 -> ShiftType.MORNING_8H;
                case 1 -> ShiftType.AFTERNOON_8H;
                default -> ShiftType.NIGHT_8H;
            };
        }

        // Standard fixed 8h shift
        return ShiftType.MORNING_8H;
    }
}
