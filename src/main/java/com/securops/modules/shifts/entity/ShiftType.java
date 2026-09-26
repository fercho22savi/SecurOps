package com.securops.modules.shifts.entity;

public enum ShiftType {
    DAY_12H,        // Diurno 12h (06:00 - 18:00)
    NIGHT_12H,      // Nocturno 12h (18:00 - 06:00)
    MORNING_8H,     // Mañana 8h (06:00 - 14:00)
    AFTERNOON_8H,   // Tarde 8h (14:00 - 22:00)
    NIGHT_8H,       // Noche 8h (22:00 - 06:00)
    OFF_DUTY        // Descanso / Libre
}
