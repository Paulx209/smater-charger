package com.smartcharger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 年度充电统计响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargingStatisticsYearlyResponse {

    /**
     * 年份
     */
    private Integer year;

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
     * 每月统计记录
     */
    private List<MonthlyRecord> records;

    /**
     * 每月统计记录内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRecord {
        /**
         * 月份（1-12）
         */
        private Integer month;

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
