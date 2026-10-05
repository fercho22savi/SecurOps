package com.securops.modules.guards.entity;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum GuardOperationalRole {
    SECURITY_GUARD("Vigilante / Guarda Estándar", new BigDecimal("1600000.00"), false, false),
    POST_LEADER_SUPERVISOR("Guarda Líder / Supervisor de Puesto", new BigDecimal("1900000.00"), true, false),
    POST_COORDINATOR("Coordinador de Puesto", new BigDecimal("2400000.00"), true, false),
    CCTV_TECH_OPERATOR("Operador de Medios Tecnológicos (CCTV)", new BigDecimal("1850000.00"), false, true);

    private final String displayName;
    private final BigDecimal defaultBaseSalary;
    private final boolean isLeadershipRole;
    private final boolean isCctvOperator;

    GuardOperationalRole(String displayName, BigDecimal defaultBaseSalary, boolean isLeadershipRole, boolean isCctvOperator) {
        this.displayName = displayName;
        this.defaultBaseSalary = defaultBaseSalary;
        this.isLeadershipRole = isLeadershipRole;
        this.isCctvOperator = isCctvOperator;
    }
}
