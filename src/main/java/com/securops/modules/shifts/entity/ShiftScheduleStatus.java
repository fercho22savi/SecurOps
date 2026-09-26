package com.securops.modules.shifts.entity;

public enum ShiftScheduleStatus {
    SCHEDULED,    // Programado en malla
    CONFIRMED,    // Confirmado por guarda/supervisor
    IN_PROGRESS,  // Guarda en puesto (marcó entrada)
    COMPLETED,    // Turno cumplido y relevado
    UNCOVERED,    // Puesto descubierto (alerta operativa crítica)
    REPLACED      // Cubierto por guarda de relevo por novedad
}
