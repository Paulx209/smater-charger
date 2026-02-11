package com.smartcharger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatisticsResponse {

    private String rangeType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalRevenue;
    private BigDecimal averageDailyRevenue;
    private Integer totalChargingCount;
    private List<DailyRevenueRecord> dailyRecords;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRevenueRecord {
        private String date;
        private BigDecimal revenue;
        private Integer chargingCount;
    }
}
