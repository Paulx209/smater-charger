package com.smartcharger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 月度充电统计响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargingStatisticsMonthlyResponse {

    /**
     * 年份
     */
    private Integer year;

    /**
     * 月份
     */
    private Integer month;

    /**
     * 总次数
     */
    private Integer totalCount;

    /**
     * 总充电量（度）
     */
    private BigDecimal totalElectricQuantity;

    /**
     * 总费用（元）
     */
    private BigDecimal totalFee;

    /**
     * 每日统计记录
     */
    private List<DailyRecord> records;

    /**
     * 每日统计记录内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRecord {
        /**
         * 日期（格式：2026-01-24）
         */
        private String date;

        /**
         * 充电次数
         */
        private Integer count;

        /**
         * 充电量（度）
         */
        private BigDecimal electricQuantity;

        /**
         * 费用（元）
         */
        private BigDecimal fee;
    }
}
