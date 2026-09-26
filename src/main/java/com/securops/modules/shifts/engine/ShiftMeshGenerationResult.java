package com.securops.modules.shifts.engine;

import com.securops.modules.shifts.entity.ShiftSchedule;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ShiftMeshGenerationResult {
    private int totalSchedulesGenerated;
    private int totalWorkingShifts;
    private int totalRestDays;
    private int uncoveredSlotsCount;
    private List<ShiftSchedule> schedules;
    private List<String> operationalAlerts;
}
