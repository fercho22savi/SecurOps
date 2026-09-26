package com.securops.modules.posts.entity;

public enum ServiceCoverageType {
    CONTINUOUS_24_7,       // Requires full 24h coverage 7 days/week (e.g. 2x2x2 or 3x3)
    DAYTIME_12H,           // 12 hours daytime only (e.g. 06:00 - 18:00)
    NIGHTTIME_12H,         // 12 hours nighttime only (e.g. 18:00 - 06:00)
    OFFICE_8H_5X2,         // 8 hours Mon-Fri (08:00 - 17:00 / 06:00 - 14:00)
    CONTINUOUS_8H_3_SHIFTS // 24 hours covered by 3 x 8h shifts (Morning, Afternoon, Night)
}
