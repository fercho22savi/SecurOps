package com.securops.modules.shifts.entity;

public enum RotationPattern {
    ROTATION_2X2X2_12H,  // 2 Day 12h, 2 Night 12h, 2 Off (Cycle: 6 days)
    ROTATION_3X3_12H,    // 3 Day 12h, 3 Off (Cycle: 6 days) or 3D, 3N, 3 Off (Cycle: 9 days)
    ROTATION_4X4_12H,    // 4 Working 12h, 4 Off (Cycle: 8 days)
    ROTATION_6X1_8H,     // 6 Days 8h, 1 Off (Cycle: 7 days)
    ROTATION_5X2_8H      // 5 Days 8h (Mon-Fri), 2 Off (Sat-Sun) (Cycle: 7 days)
}
