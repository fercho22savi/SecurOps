package com.securops.modules.guards.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class GuardMetricsDto {
    private Long guardId;
    private String guardName;
    private BigDecimal overallScore; // 0 - 100
    private double attendancePercentage;
    private double punctualityPercentage;
    private double callCompliancePercentage;
    private long totalScheduledShifts;
    private long delaysCount;
    private long disciplinaryActionsCount;
    private String performanceTier; // EXCELLENT (90-100), GOOD (75-89), FAIR (60-74), AT_RISK (<60)
}
