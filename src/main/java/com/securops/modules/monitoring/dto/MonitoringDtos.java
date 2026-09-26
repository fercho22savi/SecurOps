package com.securops.modules.monitoring.dto;

import com.securops.modules.monitoring.entity.CallStatus;
import com.securops.modules.monitoring.entity.NoveltyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class MonitoringDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ControlCallRequestDto {
        private Long postId;
        private Long guardId;
        private LocalDateTime scheduledCallTime;
        private LocalDateTime actualCallTime;
        private CallStatus status;
        private String logMinuteNote;
        private Long operatorUserId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NoveltyReportRequestDto {
        private Long guardId;
        private Long postId;
        private NoveltyType type;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        @Builder.Default
        private boolean impactsPayroll = true;
        @Builder.Default
        private int payrollDiscountHours = 0;
        @Builder.Default
        private boolean triggersDisciplinaryAction = false;
        private String description;
        private Long approvedByUserId;
        private String supportDocUrl;
    }
}
