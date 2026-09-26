package com.securops.modules.shifts.engine;

import com.securops.modules.guards.entity.Guard;
import com.securops.modules.posts.entity.SecurityPost;
import com.securops.modules.shifts.entity.RotationScheme;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Getter
@Builder
public class ShiftMeshGenerationRequest {
    private SecurityPost securityPost;
    private RotationScheme rotationScheme;
    private List<Guard> assignedGuards;
    private YearMonth targetMonth;
    private LocalDate cycleAnchorDate; // Base reference date where cycle day = 0
}
