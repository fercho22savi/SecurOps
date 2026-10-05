package com.securops.modules.guards.dto;

import com.securops.modules.guards.entity.GuardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GuardDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateGuardRequestDto {
        private String nationalId;
        private String firstName;
        private String lastName;
        private String phone;
        private String email;
        private String professionalLicense;
        private boolean certifiedFirearms;
        private LocalDate firearmsLicenseExpiry;
        private com.securops.modules.guards.entity.GuardOperationalRole operationalRole;
        private BigDecimal baseSalary;
        private boolean cctvCertified;
        private String username;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeactivateGuardRequestDto {
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuardResponseDto {
        private Long id;
        private String nationalId;
        private String fullName;
        private String firstName;
        private String lastName;
        private String phone;
        private String email;
        private String professionalLicense;
        private boolean certifiedFirearms;
        private LocalDate firearmsLicenseExpiry;
        private GuardStatus status;
        private com.securops.modules.guards.entity.GuardOperationalRole operationalRole;
        private String operationalRoleName;
        private BigDecimal baseSalary;
        private boolean cctvCertified;
        private boolean isLeadershipRole;
        private BigDecimal performanceScore;
        private String deactivationReason;
        private LocalDate deactivationDate;
        private LocalDateTime createdAt;
        private boolean availableForShifts;
    }
}
